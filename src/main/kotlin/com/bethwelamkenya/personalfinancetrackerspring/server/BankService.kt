package com.bethwelamkenya.personalfinancetrackerspring.server

import com.bethwelamkenya.personalfinancetrackerspring.AppException
import com.bethwelamkenya.personalfinancetrackerspring.domain.*
import org.springframework.stereotype.Service

@Service
class BankService(
    private val banksRepository: BanksRepository,
    private val encryptionHelper: FirebaseEncryptionHelper
) {
    fun createBankAccount(id: String, bankAccount: BankAccount): BankAccount {
        if (bankAccount.accountNumber.isNullOrBlank() || bankAccount.bankName.isNullOrBlank()) {
            throw AppException.InsufficientDetails() // 🔥 Throw exception
        }
        val accountId = bankAccount.accountNumber?.let { encryptionHelper.hashForFirebase(it) }

        // Check if user already exists
        if (accountId == null || banksRepository.existsById(accountId)) {
            throw AppException.BankAccountAlreadyExists()
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
                userEmail = id,
                balance = balance
            )
        return banksRepository.save(savedAccount).copy(
            accountNumber = bankAccount.accountNumber,
            holderName = bankAccount.holderName
        )
    }

    fun updateBankAccount(id: String, bankAccount: BankAccount): BankAccount {
        val existingAccount = banksRepository.findById(id).orElseThrow { AppException.BankAccountNotFound() }

        val newAccountId = bankAccount.accountNumber?.let { encryptionHelper.hashForFirebase(it) } ?: existingAccount.id
        val encrypterUserEmail =
            bankAccount.userEmail?.let { encryptionHelper.hashForFirebase(it) } ?: existingAccount.userEmail
        val encryptedAccountNumber =
            bankAccount.accountNumber?.let { encryptionHelper.encryptForFirebase(it) } ?: existingAccount.accountNumber
        val encryptedHolderName =
            bankAccount.holderName?.let { encryptionHelper.encryptForFirebase(it) } ?: existingAccount.holderName
        val balance = bankAccount.balance ?: existingAccount.balance
        val currency = bankAccount.currency ?: existingAccount.currency
        val bankName = bankAccount.bankName ?: existingAccount.bankName

        // Update only non-null fields
        val updatedUser = existingAccount.copy(
            id = newAccountId,
            accountNumber = encryptedAccountNumber,
            holderName = encryptedHolderName,
            currency = currency,
            userEmail = encrypterUserEmail,
            bankName = bankName,
            balance = balance
        )
        return banksRepository.save(updatedUser)
    }

    fun getBankAccountById(id: String): BankAccount {
        val account = banksRepository.findById(id).orElseThrow { AppException.BankAccountNotFound() }
        return account.copy(
            accountNumber = account.accountNumber?.let { encryptionHelper.decryptFromFirebase(it) },
            holderName = account.holderName?.let {
                encryptionHelper.decryptFromFirebase(it)
            })
    }

    fun getBankAccountByAccountNumber(accountNumber: String): BankAccount {
        val accountId = encryptionHelper.hashForFirebase(accountNumber)
        val account = banksRepository.findById(accountId).orElseThrow { AppException.BankAccountNotFound() }
        return account.copy(
            accountNumber = account.accountNumber?.let { encryptionHelper.decryptFromFirebase(it) },
            holderName = account.holderName?.let {
                encryptionHelper.decryptFromFirebase(it)
            })
    }

    fun getAllAccounts(id: String): List<BankAccount> {
        return banksRepository.findAll().filter { it.userEmail == id }.map {
            it.copy(
                accountNumber = it.accountNumber?.let { number -> encryptionHelper.decryptFromFirebase(number) },
                holderName = it.holderName?.let { name ->
                    encryptionHelper.decryptFromFirebase(name)
                }
            )
        }
    }

    fun deleteAccount(id: String) {
        val savingsGoal = banksRepository.findById(id).orElseThrow { AppException.BankAccountNotFound() }
        banksRepository.delete(savingsGoal)
    }

    fun transact(transaction: Transaction) {
        if (transaction.accountNumber.isNullOrBlank() || transaction.type.isNullOrBlank() || transaction.amount?.let { it <= 0.0 } != false) {
            throw AppException.InsufficientDetails() // 🔥 Throw exception
        }
        val bankAccount = transaction.accountNumber?.let {
            banksRepository.findById(it).orElseThrow { AppException.BankAccountNotFound() }
        } ?: throw AppException.InsufficientDetails()
        val finalBalance: Double
        when (transaction.type) {
            TransactionType.WITHDRAW.name, TransactionType.DEPOSIT_GOAL.name,
            TransactionType.TRANSFER_OUT.name, TransactionType.TRANSFER_OUT_TO.name -> {
                if (bankAccount.balance!! < transaction.amount!!){
                    throw AppException.InsufficientFunds()
                }
                finalBalance = bankAccount.balance!! - transaction.amount!!
            }

            TransactionType.DEPOSIT.name, TransactionType.WITHDRAW_GOAL.name,
            TransactionType.TRANSFER_IN.name, TransactionType.TRANSFER_IN_FROM.name -> {
                finalBalance = bankAccount.balance!! + transaction.amount!!
            }

            else -> {
                throw AppException.InsufficientDetails()
            }
        }
        banksRepository.save(bankAccount.copy(balance = finalBalance))
    }
}
