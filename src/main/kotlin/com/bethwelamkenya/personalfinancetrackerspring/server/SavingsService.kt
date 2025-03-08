package com.bethwelamkenya.personalfinancetrackerspring.server

import com.bethwelamkenya.personalfinancetrackerspring.AppException
import com.bethwelamkenya.personalfinancetrackerspring.domain.*
import org.springframework.stereotype.Service

@Service
class SavingsService(
    private val savingsRepository: SavingsRepository,
    private val encryptionHelper: FirebaseEncryptionHelper
) {
    fun createSavingsGoal(id: String, savingsGoal: SavingsGoal): SavingsGoal {
        if (savingsGoal.accountNumber.isNullOrBlank() || savingsGoal.goalName.isNullOrBlank()) {
            throw AppException.InsufficientDetails() // 🔥 Throw exception
        }
        val goalId = savingsGoal.goalName?.let { encryptionHelper.hashForFirebase(it) }
            ?: throw AppException.InsufficientDetails()
        val existingGoals = savingsRepository.findAllById(listOf(goalId)).filter {
            it.userEmail == id
        }

        // Check if user already exists
        if (existingGoals.isNotEmpty()) {
            throw AppException.SavingsGoalAlreadyExists()
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
                userEmail = id,
                savedAmount = savedAmount
            )
        return savingsRepository.save(savedGoal).copy(
            accountNumber = savingsGoal.accountNumber,
            goalName = savingsGoal.goalName
        )
    }

    fun updateSavingsGoal(id: String, savingsGoal: SavingsGoal): SavingsGoal {
        val existingAccount = savingsRepository.findById(id).orElseThrow { AppException.SavingsGoalNotFound() }

        val newGoalId = savingsGoal.goalName?.let { encryptionHelper.hashForFirebase(it) } ?: existingAccount.id
        val encryptedUserEmail =
            savingsGoal.userEmail?.let { encryptionHelper.hashForFirebase(it) } ?: existingAccount.userEmail
        val encryptedAccountNumber =
            savingsGoal.accountNumber?.let { encryptionHelper.encryptForFirebase(it) } ?: existingAccount.accountNumber
        val encryptedGoalName =
            savingsGoal.goalName?.let { encryptionHelper.encryptForFirebase(it) } ?: existingAccount.goalName
        val savedAmount = savingsGoal.savedAmount ?: existingAccount.savedAmount
        val targetAmount = savingsGoal.targetAmount ?: existingAccount.targetAmount
        val currency = savingsGoal.currency ?: existingAccount.currency

        // Update only non-null fields
        val updatedUser = existingAccount.copy(
            id = newGoalId,
            accountNumber = encryptedAccountNumber,
            goalName = encryptedGoalName,
            currency = currency,
            userEmail = encryptedUserEmail,
            targetAmount = targetAmount,
            savedAmount = savedAmount
        )
        return savingsRepository.save(updatedUser)
    }

    fun getSavingsGoalById(id: String): SavingsGoal {
        val account = savingsRepository.findById(id).orElseThrow { AppException.BankAccountNotFound() }
        return account.copy(
            accountNumber = account.accountNumber?.let { encryptionHelper.decryptFromFirebase(it) },
            goalName = account.goalName?.let {
                encryptionHelper.decryptFromFirebase(it)
            })
    }

    fun getSavingsGoalByGoalName(accountNumber: String): SavingsGoal {
        val accountId = encryptionHelper.hashForFirebase(accountNumber)
        val account = savingsRepository.findById(accountId).orElseThrow { AppException.BankAccountNotFound() }
        return account.copy(
            accountNumber = account.accountNumber?.let { encryptionHelper.decryptFromFirebase(it) },
            goalName = account.goalName?.let {
                encryptionHelper.decryptFromFirebase(it)
            })
    }

    fun getAllSavings(id: String): List<SavingsGoal> {
        return savingsRepository.findAll().filter { it.userEmail == id }.map {
            it.copy(
                accountNumber = it.accountNumber?.let { number -> encryptionHelper.decryptFromFirebase(number) },
                goalName = it.goalName?.let { name ->
                    encryptionHelper.decryptFromFirebase(name)
                }

            )
        }
    }

    fun deleteSaving(id: String) {
        val savingsGoal = savingsRepository.findById(id).orElseThrow { AppException.SavingsGoalNotFound() }
        savingsRepository.delete(savingsGoal)
    }

    fun transact(transaction: Transaction) {
        if (transaction.accountNumber.isNullOrBlank() || transaction.type.isNullOrBlank() || transaction.amount?.let { it <= 0.0 } != false) {
            throw AppException.InsufficientDetails() // 🔥 Throw exception
        }
        val savingsGoals = transaction.goalName?.let {
            savingsRepository.findAllById(listOf(it)).filter { goal -> goal.userEmail == transaction.userEmail }
        } ?: throw AppException.InsufficientDetails()
        if (savingsGoals.isEmpty()) throw AppException.SavingsGoalNotFound()
        val savingsGoal = savingsGoals[0]
        val finalBalance: Double
        when (transaction.type) {
            TransactionType.WITHDRAW_GOAL.name, TransactionType.TRANSFER_GOAL_OUT.name,
            TransactionType.TRANSFER_GOAL_OUT_TO.name -> {
                if (savingsGoal.savedAmount!! < transaction.amount!!) {
                    throw AppException.InsufficientFunds()
                }
                finalBalance = savingsGoal.savedAmount!! - transaction.amount!!
            }

            TransactionType.DEPOSIT_GOAL.name, TransactionType.TRANSFER_GOAL_IN.name,
            TransactionType.TRANSFER_GOAL_IN_FROM.name -> {
                finalBalance = savingsGoal.savedAmount!! + transaction.amount!!
            }

            else -> {
                throw AppException.InsufficientDetails()
            }
        }
        savingsRepository.save(savingsGoal.copy(savedAmount = finalBalance))
    }
}
