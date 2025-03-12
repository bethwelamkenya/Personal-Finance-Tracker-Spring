package com.bethwelamkenya.personalfinancetrackerspring.server

import com.bethwelamkenya.personalfinancetrackerspring.AppException
import com.bethwelamkenya.personalfinancetrackerspring.domain.*
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Service
import javax.xml.transform.Source

@Service
class FirebaseService(
    private val encryptionHelper: FirebaseEncryptionHelper
) {
    private final val db: Firestore = FirestoreClient.getFirestore()
//    init {
//        // Disable network to allow for cache clearing
//        db.disableNetwork().get();
//
//// Clear persistence
//        db.clearPersistence().get();
//
//// Re-enable network
//        db.enableNetwork().get();
//    }
    private final val usersCollection = "bank_accounts"
    private final val banksCollection = "accounts"
    private final val savingsCollection = "savings"
    private final val transactionsCollection = "transactions"

    fun createUser(user: User): User {
        if (user.email.isNullOrBlank() || user.name.isNullOrBlank() || user.passwordHash.isNullOrBlank()) {
            throw AppException.InsufficientDetails()
        }

        val userId =
            user.email?.let { encryptionHelper.hashForFirebase(it) } ?: throw AppException.InsufficientDetails()

        // Check if user already exists
        val userDocument = db.collection(usersCollection).document(userId).get().get()

        if (userDocument.exists()) {
            throw AppException.UserAlreadyExists(user.email)
        }

        val encryptedName = user.name?.let { encryptionHelper.encryptForFirebase(it) }
        val encryptedEmail = user.email?.let { encryptionHelper.encryptForFirebase(it) }
        val salt = encryptionHelper.generateSalt()
        val hashedPassword = user.passwordHash?.let { encryptionHelper.hashPassword(it, salt) }

        val savedUser =
            User(id = userId, name = encryptedName, email = encryptedEmail, passwordHash = hashedPassword, salt = salt)
        db.collection(usersCollection).document(userId).set(user).get() ?: throw AppException.UnknownError()
        return savedUser.copy(
            name = user.name,
            email = user.email
        )
    }

    fun logInUser(user: User): User {
        val id = user.email?.let { encryptionHelper.hashForFirebase(it) } ?: throw AppException.InsufficientDetails()
        if (user.passwordHash.isNullOrBlank()) {
            throw AppException.InsufficientDetails()
        }
        val savedDocument = db.collection(usersCollection).document(id).get().get() ?: throw AppException.UserNotFound()
        val savedUser = savedDocument.toObject(User::class.java) ?: throw AppException.UserNotFound()

        val salt = savedUser.salt!!
        val passwordHash = user.passwordHash?.let { encryptionHelper.hashPassword(password = it, salt = salt) }

        if (passwordHash != savedUser.passwordHash) {
            throw AppException.InvalidPassword()
        }
        val decryptedUser = savedUser.copy(
            id = savedDocument.id,
            name = savedUser.name?.let { encryptionHelper.decryptFromFirebase(it) },
            email = user.email
        )
        return decryptedUser
    }

    fun updateUser(id: String, user: User): User {
        val existingDocument =
            db.collection(usersCollection).document(id).get().get() ?: throw AppException.UserNotFound()
        val existingUser = existingDocument.toObject(User::class.java) ?: throw AppException.UserNotFound()

        val encryptedName = user.name?.let { encryptionHelper.encryptForFirebase(it) } ?: existingUser.name
        val salt = user.passwordHash?.let { encryptionHelper.generateSalt() } ?: existingUser.salt
        val hashedPassword =
            user.passwordHash?.let { encryptionHelper.hashPassword(it, salt!!) } ?: existingUser.passwordHash

        val updatedUser = existingUser.copy(
            name = encryptedName,
            passwordHash = hashedPassword,
            salt = salt
        )
        db.collection(usersCollection).document(id).set(updatedUser).get() ?: throw AppException.UnknownError()
        return updatedUser.copy(
            name = updatedUser.name?.let { encryptionHelper.decryptFromFirebase(it) },
            email = updatedUser.email?.let {
                encryptionHelper.decryptFromFirebase(it)
            }
        )
    }

    fun getUserById(id: String): User {
        val userDocument = db.collection(usersCollection).document(id).get().get() ?: throw AppException.UserNotFound()
        val user = userDocument.toObject(User::class.java) ?: throw AppException.UserNotFound()
        return user.copy(
            name = user.name?.let { encryptionHelper.decryptFromFirebase(it) },
            email = user.email?.let {
                encryptionHelper.decryptFromFirebase(it)
            })
    }

    fun getUserByEmail(email: String): User {
        val userId = encryptionHelper.hashForFirebase(email) ?: throw AppException.InsufficientDetails()
        return getUserById(userId)
    }

    fun deleteUser(id: String) {
        db.collection(usersCollection).document(id).delete().get() ?: throw AppException.UserNotFound()
    }

    fun createBankAccount(id: String, bankAccount: BankAccount): BankAccount {
        if (bankAccount.accountNumber.isNullOrBlank() || bankAccount.bankName.isNullOrBlank()) {
            throw AppException.InsufficientDetails()
        }
        val accountId = bankAccount.accountNumber?.let { encryptionHelper.hashForFirebase(it) }
            ?: throw AppException.InsufficientDetails()

        // Check if account already exists
        val accountDocument =
            db.collection(usersCollection).document(id).collection(banksCollection).document(accountId).get().get()

        if (accountDocument.exists()) {
            throw AppException.BankAccountAlreadyExists(bankAccount.accountNumber)
        }

        val encryptedAccountNumber = bankAccount.accountNumber?.let { encryptionHelper.encryptForFirebase(it) }
        val encryptedHolderName = bankAccount.holderName?.let { encryptionHelper.encryptForFirebase(it) }
        val balance = bankAccount.balance ?: 0.0
        val currency = bankAccount.currency ?: CurrencyType.USD.code

        val savedAccount =
            BankAccount(
                id = accountId,
                accountNumber = encryptedAccountNumber,
                holderName = encryptedHolderName,
                bankName = bankAccount.bankName,
                currency = currency,
                balance = balance
            )
        db.collection(usersCollection).document(id).collection(banksCollection).document(accountId)
            .set(savedAccount).get() ?: throw AppException.UnknownError()
        return savedAccount.copy(
            accountNumber = bankAccount.accountNumber,
            holderName = bankAccount.holderName
        )
    }

    fun updateBankAccount(id: String, bankAccount: BankAccount): BankAccount {
        val existingAccountDocument =
            bankAccount.id?.let { db.collection(usersCollection).document(id).collection(banksCollection).document(it).get().get() }
                ?: throw AppException.BankAccountNotFound()
        val existingAccount =
            existingAccountDocument.toObject(BankAccount::class.java) ?: throw AppException.UserNotFound()

        val encryptedHolderName =
            bankAccount.holderName?.let { encryptionHelper.encryptForFirebase(it) } ?: existingAccount.holderName
        val balance = bankAccount.balance ?: existingAccount.balance
        val currency = bankAccount.currency ?: existingAccount.currency
        val bankName = bankAccount.bankName ?: existingAccount.bankName

        // Update only non-null fields
        val updatedAccount = existingAccount.copy(
            holderName = encryptedHolderName,
            currency = currency,
            bankName = bankName,
            balance = balance
        )

        db.collection(usersCollection).document(id).collection(banksCollection).document(bankAccount.id)
            .set(updatedAccount).get() ?: throw AppException.UnknownError()
        return updatedAccount.copy(
            holderName = updatedAccount.holderName?.let { encryptionHelper.decryptFromFirebase(it) },
            accountNumber = updatedAccount.accountNumber?.let {
                encryptionHelper.decryptFromFirebase(it)
            }
        )
    }

    fun getBankAccountById(emailId: String, bankId: String): BankAccount {
        val accountDocument =
            db.collection(usersCollection).document(emailId).collection(banksCollection).document(bankId).get().get()
                ?: throw AppException.BankAccountNotFound()
        val account = accountDocument.toObject(BankAccount::class.java) ?: throw AppException.BankAccountNotFound()
        return account.copy(
            accountNumber = account.accountNumber?.let { encryptionHelper.decryptFromFirebase(it) },
            holderName = account.holderName?.let {
                encryptionHelper.decryptFromFirebase(it)
            })
    }

    fun getBankAccountByAccountNumber(emailId: String, accountNumber: String): BankAccount {
        val accountId = encryptionHelper.hashForFirebase(accountNumber) ?: throw AppException.InsufficientDetails()
        return getBankAccountById(emailId, accountId)
    }

    fun getAllAccounts(id: String): List<BankAccount> {
        val result = db.collection(usersCollection).document(id).collection(banksCollection).listDocuments()
        var accounts = result.mapNotNull {
            it.get().get().toObject(BankAccount::class.java)
        }
        accounts = accounts.map {
            it.copy(
                holderName = it.holderName?.let { name -> encryptionHelper.decryptFromFirebase(name) },
                accountNumber = it.accountNumber?.let { number ->
                    encryptionHelper.decryptFromFirebase(number)
                }
            )
        }
        if (accounts.isEmpty()) {
            throw AppException.BankAccountNotFound()
        }
        return accounts
    }

    fun deleteAccount(emailId: String, bankId: String) {
        db.collection(usersCollection).document(emailId).collection(banksCollection).document(bankId).delete().get()
            ?: throw AppException.BankAccountNotFound()
    }

    fun createSavingsGoal(id: String, savingsGoal: SavingsGoal): SavingsGoal {
        if (savingsGoal.accountNumber.isNullOrBlank() || savingsGoal.goalName.isNullOrBlank()) {
            throw AppException.InsufficientDetails()
        }
        val goalId = savingsGoal.goalName?.let { encryptionHelper.hashForFirebase(it) }
            ?: throw AppException.InsufficientDetails()

        // Check if goal already exists
        val goalDocument =
            db.collection(usersCollection).document(id).collection(savingsCollection).document(goalId).get().get()

        if (goalDocument.exists()) {
            throw AppException.SavingsGoalAlreadyExists(savingsGoal.goalName)
        }

        val encryptedAccountNumber = savingsGoal.accountNumber?.let { encryptionHelper.encryptForFirebase(it) }
        val encryptedGoalName = savingsGoal.goalName?.let { encryptionHelper.encryptForFirebase(it) }
        val savedAmount = savingsGoal.savedAmount ?: 0.0
        val targetAmount = savingsGoal.targetAmount ?: 0.0
        val currency = savingsGoal.currency ?: CurrencyType.USD.code

        val savedGoal =
            SavingsGoal(
                id = goalId,
                accountNumber = encryptedAccountNumber,
                goalName = encryptedGoalName,
                targetAmount = targetAmount,
                currency = currency,
                savedAmount = savedAmount
            )
        db.collection(usersCollection).document(id).collection(savingsCollection).document(goalId)
            .set(savedGoal).get() ?: throw AppException.UnknownError()
        return savedGoal.copy(
            accountNumber = savingsGoal.accountNumber,
            goalName = savingsGoal.goalName
        )
    }

    fun updateSavingsGoal(id: String, savingsGoal: SavingsGoal): SavingsGoal {
        val existingAccountDocument =
            savingsGoal.id?.let {
                db.collection(usersCollection).document(id).collection(savingsCollection).document(it).get()
                    .get()
            } ?: throw AppException.SavingsGoalNotFound()
        val existingAccount =
            existingAccountDocument.toObject(SavingsGoal::class.java) ?: throw AppException.SavingsGoalNotFound()

        val encryptedAccountNumber =
            savingsGoal.accountNumber?.let { encryptionHelper.encryptForFirebase(it) } ?: existingAccount.accountNumber
        val savedAmount = savingsGoal.savedAmount ?: existingAccount.savedAmount
        val targetAmount = savingsGoal.targetAmount ?: existingAccount.targetAmount
        val currency = savingsGoal.currency ?: existingAccount.currency

        // Update only non-null fields
        val updatedGoal = existingAccount.copy(
            accountNumber = encryptedAccountNumber,
            currency = currency,
            targetAmount = targetAmount,
            savedAmount = savedAmount
        )
        updatedGoal.id?.let {
            db.collection(usersCollection).document(id).collection(savingsCollection).document(it)
                .set(updatedGoal).get()
        } ?: throw AppException.UnknownError()
        return updatedGoal.copy(
            goalName = updatedGoal.goalName?.let { encryptionHelper.decryptFromFirebase(it) },
            accountNumber = updatedGoal.accountNumber?.let {
                encryptionHelper.decryptFromFirebase(it)
            }
        )
    }

    fun getSavingsGoalById(emailId: String, goalId: String): SavingsGoal {
        val goalDocument =
            db.collection(usersCollection).document(emailId).collection(savingsCollection).document(goalId).get().get()
                ?: throw AppException.SavingsGoalNotFound()
        val goal = goalDocument.toObject(SavingsGoal::class.java) ?: throw AppException.SavingsGoalNotFound()
        return goal.copy(
            accountNumber = goal.accountNumber?.let { encryptionHelper.decryptFromFirebase(it) },
            goalName = goal.goalName?.let {
                encryptionHelper.decryptFromFirebase(it)
            })
    }

    fun getSavingsGoalByGoalName(emailId: String, goalName: String): SavingsGoal {
        val goalId = encryptionHelper.hashForFirebase(goalName) ?: throw AppException.InsufficientDetails()
        return getSavingsGoalById(emailId, goalId)
    }

    fun getAllSavings(id: String): List<SavingsGoal> {
        val result = db.collection(usersCollection).document(id).collection(savingsCollection).listDocuments()
        var goals = result.mapNotNull {
            it.get().get().toObject(SavingsGoal::class.java)
        }
        goals = goals.map {
            it.copy(
                accountNumber = it.accountNumber?.let { number -> encryptionHelper.decryptFromFirebase(number) },
                goalName = it.goalName?.let { name ->
                    encryptionHelper.decryptFromFirebase(name)
                }
            )
        }
        if (goals.isEmpty()) {
            throw AppException.SavingsGoalNotFound()
        }
        return goals
    }

    fun deleteSaving(emailId: String, goalId: String) {
        db.collection(usersCollection).document(emailId).collection(savingsCollection).document(goalId).delete().get()
            ?: throw AppException.BankAccountNotFound()
    }

    fun getTransactionById(emailId: String, transactionId: String): Transaction {
        val transactionDocument =
            db.collection(usersCollection).document(emailId).collection(transactionsCollection).document(transactionId)
                .get().get()
                ?: throw AppException.SavingsGoalNotFound()
        val transaction =
            transactionDocument.toObject(Transaction::class.java) ?: throw AppException.TransactionNotFound()
        return transaction.copy(
            goalName = transaction.goalName?.let { name -> encryptionHelper.decryptFromFirebase(name) },
            targetGoalName = transaction.targetGoalName?.let { name -> encryptionHelper.decryptFromFirebase(name) },
            accountNumber = transaction.accountNumber?.let { number -> encryptionHelper.decryptFromFirebase(number) },
            targetUserEmail = transaction.targetUserEmail?.let { number -> encryptionHelper.decryptFromFirebase(number) },
            targetAccountNumber = transaction.targetAccountNumber?.let { number ->
                encryptionHelper.decryptFromFirebase(number)
            })
    }

    fun getAllTransactions(id: String): List<Transaction> {
        val result = db.collection(usersCollection).document(id).collection(transactionsCollection).listDocuments()
        var transactions = result.mapNotNull {
            it.get().get().toObject(Transaction::class.java)
        }
        transactions = transactions.map {
            it.copy(
                goalName = it.goalName?.let { name -> encryptionHelper.decryptFromFirebase(name) },
                targetGoalName = it.targetGoalName?.let { name -> encryptionHelper.decryptFromFirebase(name) },
                accountNumber = it.accountNumber?.let { number -> encryptionHelper.decryptFromFirebase(number) },
                targetUserEmail = it.targetUserEmail?.let { number -> encryptionHelper.decryptFromFirebase(number) },
                targetAccountNumber = it.targetAccountNumber?.let { number ->
                    encryptionHelper.decryptFromFirebase(number)
                }
            )
        }
        if (transactions.isEmpty()) {
            throw AppException.TransactionNotFound()
        }
        return transactions
    }

    fun deleteTransaction(emailId: String, transactionId: String) {
        db.collection(usersCollection).document(emailId).collection(transactionsCollection).document(transactionId)
            .delete().get()
            ?: throw AppException.TransactionNotFound()
    }

    fun createTransaction(
        id: String,
        transaction: Transaction,
    ): Transaction {
        if (transaction.accountNumber.isNullOrBlank() || transaction.type.isNullOrBlank() || transaction.amount?.let { it <= 0.0 } != false) {
            throw AppException.InsufficientDetails()
        }

        db.collection(usersCollection).document(id).get().get() ?: throw AppException.UserNotFound()
        val targetUserEmail = transaction.targetUserEmail?.let { encryptionHelper.hashForFirebase(it) }
        val encryptedAccountNumber = transaction.accountNumber?.let { encryptionHelper.encryptForFirebase(it) }
        val encryptedGoalName = transaction.goalName?.let { encryptionHelper.encryptForFirebase(it) }
        val encryptedTargetAccountNumber =
            transaction.targetAccountNumber?.let { encryptionHelper.encryptForFirebase(it) }
        val encryptedTargetGoalName = transaction.targetGoalName?.let { encryptionHelper.encryptForFirebase(it) }
        val currency = transaction.currency ?: CurrencyType.USD.code

        val savedTransaction = Transaction(
            type = transaction.type,
            accountNumber = encryptedAccountNumber,
            targetAccountNumber = encryptedTargetAccountNumber,
            targetUserEmail = targetUserEmail,
            goalName = encryptedGoalName,
            targetGoalName = encryptedTargetGoalName,
            currency = currency,
            amount = transaction.amount
        )

        when (savedTransaction.type) {
            TransactionType.WITHDRAW.name,
            TransactionType.DEPOSIT.name,
            -> {
                transactAccount(savedTransaction, id)
            }

            TransactionType.TRANSFER_OUT.name,
            TransactionType.TRANSFER_OUT_TO.name -> {
                transactAccount(savedTransaction, id)
                transactAccount(savedTransaction, id, true)
            }

            TransactionType.WITHDRAW_GOAL.name,
            TransactionType.DEPOSIT_GOAL.name -> {
                transactAccount(savedTransaction, id)
                transactGoal(savedTransaction, id)
            }

            TransactionType.TRANSFER_GOAL_OUT.name,
            TransactionType.TRANSFER_GOAL_OUT_TO.name,
            -> {
                transactGoal(savedTransaction, id)
                transactGoal(savedTransaction, id, true)
            }

            else -> {
                throw AppException.InvalidTransactionType()
            }
        }

        val result =
            db.collection(usersCollection).document(id).collection(transactionsCollection).add(savedTransaction).get()
                ?: throw AppException.UnknownError()
        result.id
        transaction.targetUserEmail?.takeIf { it.isNotBlank() }?.let { targetEmail ->
            db.collection(usersCollection)
                .document(targetEmail)
                .collection(transactionsCollection)
                .document(result.id)
                .set(savedTransaction).get() ?: throw AppException.UnknownError()
        }

        return savedTransaction.copy(
            accountNumber = transaction.accountNumber,
            goalName = transaction.goalName,
            targetUserEmail = transaction.targetUserEmail,
            targetAccountNumber = transaction.targetAccountNumber,
            targetGoalName = transaction.targetGoalName
        )
    }

    fun transactGoal(transaction: Transaction, id: String, into: Boolean = false) {
        if (transaction.accountNumber.isNullOrBlank() || transaction.type.isNullOrBlank() || transaction.amount?.let { it <= 0.0 } != false) {
            throw AppException.InsufficientDetails()
        }
        val hashedName: String
        val savingsGoalDoc: DocumentSnapshot
        if (into) {
            hashedName =
                transaction.targetGoalName?.takeIf {it. isNotBlank() }?.let{
                    encryptionHelper.hashForFirebase(
                        encryptionHelper.decryptFromFirebase(
                            it
                        )
                    )
                }
                    ?: throw AppException.InsufficientDetails()
            savingsGoalDoc = db.collection(usersCollection).document(
                transaction.targetUserEmail ?: id
            ).collection(savingsCollection).document(hashedName).get().get()
                ?: throw AppException.SavingsGoalNotFound()
        } else {
            hashedName =
                transaction.goalName?.let { encryptionHelper.hashForFirebase(encryptionHelper.decryptFromFirebase(it)) }
                    ?: throw AppException.InsufficientDetails()
            savingsGoalDoc =
                db.collection(usersCollection).document(id).collection(savingsCollection).document(hashedName).get()
                    .get() ?: throw AppException.SavingsGoalNotFound()
        }
        val savingsGoal = savingsGoalDoc.toObject(SavingsGoal::class.java) ?: throw AppException.SavingsGoalNotFound()
        val finalBalance: Double
        when (transaction.type) {
            TransactionType.WITHDRAW_GOAL.name -> {
                if (savingsGoal.savedAmount!! < transaction.amount!!) {
                    throw AppException.InsufficientFunds()
                }
                finalBalance = savingsGoal.savedAmount!! - transaction.amount!!
            }

            TransactionType.DEPOSIT_GOAL.name -> {
                finalBalance = savingsGoal.savedAmount!! + transaction.amount!!
            }

            TransactionType.TRANSFER_GOAL_OUT.name,
            TransactionType.TRANSFER_GOAL_OUT_TO.name -> {
                if (into) {
                    finalBalance = savingsGoal.savedAmount!! + transaction.amount!!
                } else {
                    if (savingsGoal.savedAmount!! < transaction.amount!!) {
                        throw AppException.InsufficientFunds()
                    }
                    finalBalance = savingsGoal.savedAmount!! - transaction.amount!!
                }
            }

            else -> {
                throw AppException.InvalidTransactionType()
            }
        }
        db.collection(usersCollection).document(transaction.targetUserEmail ?: id).collection(savingsCollection)
            .document(savingsGoalDoc.id)
            .set(savingsGoal.copy(savedAmount = finalBalance)).get() ?: throw AppException.UnknownError()
    }


    fun transactAccount(transaction: Transaction, id: String, into: Boolean = false) {
        if (transaction.accountNumber.isNullOrBlank() || transaction.type.isNullOrBlank() || transaction.amount?.let { it <= 0.0 } != false) {
            throw AppException.InsufficientDetails()
        }
        val hashedName: String
        val accountDoc: DocumentSnapshot
        if (into) {
            hashedName =
                transaction.targetAccountNumber?.takeIf {it.isNotBlank()}?.let{
                    encryptionHelper.hashForFirebase(
                        encryptionHelper.decryptFromFirebase(
                            it
                        )
                    )
                }
                    ?: throw AppException.InsufficientDetails()
            accountDoc = db.collection(usersCollection).document(
                transaction.targetUserEmail ?: id
            ).collection(banksCollection).document(hashedName).get().get()
                ?: throw AppException.BankAccountNotFound()
        } else {
            hashedName =
                transaction.accountNumber?.let {
                    encryptionHelper.hashForFirebase(
                        encryptionHelper.decryptFromFirebase(
                            it
                        )
                    )
                }
                    ?: throw AppException.InsufficientDetails()
            accountDoc =
                db.collection(usersCollection).document(id).collection(banksCollection).document(hashedName).get()
                    .get() ?: throw AppException.BankAccountNotFound()
        }
        val bankAccount = accountDoc.toObject(BankAccount::class.java) ?: throw AppException.BankAccountNotFound()
        val finalBalance: Double
        when (transaction.type) {
            TransactionType.WITHDRAW.name, TransactionType.DEPOSIT_GOAL.name -> {
                if (bankAccount.balance!! < transaction.amount!!) {
                    throw AppException.InsufficientFunds()
                }
                finalBalance = bankAccount.balance!! - transaction.amount!!
            }

            TransactionType.DEPOSIT.name, TransactionType.WITHDRAW_GOAL.name -> {
                finalBalance = bankAccount.balance!! + transaction.amount!!
            }

            TransactionType.TRANSFER_OUT.name, TransactionType.TRANSFER_OUT_TO.name -> {
                if (into) {
                    finalBalance = bankAccount.balance!! + transaction.amount!!
                } else {
                    if (bankAccount.balance!! < transaction.amount!!) {
                        throw AppException.InsufficientFunds()
                    }
                    finalBalance = bankAccount.balance!! - transaction.amount!!
                }
            }

            else -> {
                throw AppException.InvalidTransactionType()
            }
        }
        db.collection(usersCollection).document(transaction.targetUserEmail ?: id).collection(banksCollection)
            .document(accountDoc.id)
            .set(bankAccount.copy(balance = finalBalance)).get() ?: throw AppException.UnknownError()
    }

}

