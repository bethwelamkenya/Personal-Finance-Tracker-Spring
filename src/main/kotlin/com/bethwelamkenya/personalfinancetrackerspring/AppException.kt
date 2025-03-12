package com.bethwelamkenya.personalfinancetrackerspring

sealed class AppException(message: String) : RuntimeException(message) {
    class UserAlreadyExists(email : String? = null) : AppException("User with ${email ?: "this email"} already exists.")
    class UserNotFound : AppException("User not found.")
    class BankAccountAlreadyExists(accountNumber : String? = null) : AppException("Bank Account with ${accountNumber ?: "this account number"} already exists.")
    class BankAccountNotFound : AppException("Bank Account not found.")
    class SavingsGoalAlreadyExists(goalName : String? = null) : AppException("Savings Goal with ${goalName ?: "this goal name"} already exists.")
    class SavingsGoalNotFound : AppException("Savings Goal not found.")
    class TransactionNotFound : AppException("Transaction not found.")
    class InvalidPassword : AppException("Invalid Password.")
    class InsufficientDetails : AppException("Insufficient details provided.")
    class InsufficientFunds : AppException("Insufficient funds to complete transaction.")
    class InvalidTransactionType : AppException("Invalid transaction type.")
    class InvalidRequest : AppException("Invalid request data.")
    class UnknownError : AppException("Unknown internal error occurred.")
}
