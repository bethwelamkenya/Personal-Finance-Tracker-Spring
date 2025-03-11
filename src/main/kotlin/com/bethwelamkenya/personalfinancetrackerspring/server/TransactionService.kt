package com.bethwelamkenya.personalfinancetrackerspring.server

import com.bethwelamkenya.personalfinancetrackerspring.AppException
import com.bethwelamkenya.personalfinancetrackerspring.domain.CurrencyType
import com.bethwelamkenya.personalfinancetrackerspring.domain.FirebaseEncryptionHelper
import com.bethwelamkenya.personalfinancetrackerspring.domain.Transaction
import com.bethwelamkenya.personalfinancetrackerspring.domain.TransactionType
import org.springframework.stereotype.Service

@Service
class TransactionService(
    private val transactionsRepository: TransactionsRepository,
    private val banksRepository: BanksRepository,
    private val savingsService: SavingsService,
    private val encryptionHelper: FirebaseEncryptionHelper
) {
    fun createTransaction(
        id: String,
        transaction: Transaction,
        bankService: BankService,
        savingsService: SavingsService
    ): Transaction {
        if (transaction.accountNumber.isNullOrBlank() || transaction.type.isNullOrBlank() || transaction.amount?.let { it <= 0.0 } != false) {
            throw AppException.InsufficientDetails() // 🔥 Throw exception
        }
        val targetUserEmail = transaction.targetUserEmail?.let { encryptionHelper.hashForFirebase(it) }
        val encryptedAccountNumber = transaction.accountNumber?.let { encryptionHelper.encryptForFirebase(it) }
        val encryptedGoalName = transaction.goalName?.let { encryptionHelper.encryptForFirebase(it) }
        val encryptedTargetAccountNumber = transaction.targetAccountNumber?.let { encryptionHelper.encryptForFirebase(it) }
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
            userEmail = id,
            amount = transaction.amount
        )

        when (savedTransaction.type) {
            TransactionType.WITHDRAW.name,
            TransactionType.DEPOSIT.name,
            TransactionType.TRANSFER_OUT.name,
            TransactionType.TRANSFER_OUT_TO.name, -> {

            }

            TransactionType.TRANSFER_IN.name, -> {
                bankService.transact(savedTransaction.copy(
                    accountNumber = savedTransaction.targetAccountNumber
                ))
            }

            TransactionType.TRANSFER_IN_FROM.name -> {
                bankService.transact(savedTransaction.copy(
                    accountNumber = savedTransaction.targetAccountNumber,
                    userEmail = savedTransaction.targetUserEmail
                ))
            }

            TransactionType.WITHDRAW_GOAL.name, TransactionType.DEPOSIT_GOAL.name -> {
                bankService.transact(savedTransaction)
                savingsService.transact(savedTransaction)
            }

            TransactionType.TRANSFER_GOAL_OUT.name,
            TransactionType.TRANSFER_GOAL_OUT_TO.name, -> {
                savingsService.transact(savedTransaction)
            }

            TransactionType.TRANSFER_GOAL_IN.name, -> {
                savingsService.transact(savedTransaction.copy(
                    goalName = savedTransaction.targetGoalName
                ))
            }

            TransactionType.TRANSFER_GOAL_IN_FROM.name -> {
                savingsService.transact(savedTransaction.copy(
                    goalName = savedTransaction.targetGoalName,
                    userEmail = savedTransaction.targetUserEmail
                ))
            }

            else -> {
                throw AppException.InvalidTransactionType()
            }
        }
        return transactionsRepository.save(savedTransaction).copy(
            accountNumber = transaction.accountNumber,
        )
    }

    fun getTransactionById(id: Long): Transaction {
        val account = transactionsRepository.findById(id).orElseThrow { AppException.TransactionNotFound() }
        return account.copy(
            accountNumber = account.accountNumber?.let { encryptionHelper.decryptFromFirebase(it) },
            targetAccountNumber = account.targetAccountNumber?.let {
                encryptionHelper.decryptFromFirebase(it)
            })
    }

    fun getAllTransactions(id: String): List<Transaction> {
        return transactionsRepository.findAll().filter { it.userEmail == id || it.targetUserEmail == id }.map {
            it.copy(
                goalName = it.goalName?.let { name -> encryptionHelper.decryptFromFirebase(name) },
                targetGoalName = it.targetGoalName?.let { name -> encryptionHelper.decryptFromFirebase(name) },
                accountNumber = it.accountNumber?.let { number -> encryptionHelper.decryptFromFirebase(number) },
                targetAccountNumber = it.targetAccountNumber?.let { number ->
                    encryptionHelper.decryptFromFirebase(number)
                }
            )
        }
    }
}
