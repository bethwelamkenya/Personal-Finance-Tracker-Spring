package com.bethwelamkenya.personalfinancetrackerspring.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.google.cloud.firestore.annotation.Exclude
import com.google.cloud.firestore.annotation.ServerTimestamp
import java.time.Instant
import java.util.*

data class Transaction(
    var id: String? = "",
    var type: String? = TransactionType.DEPOSIT.name, // Store enum as a string
    var accountNumber: String? = "",
    var goalName: String? = "",
    var targetAccountNumber: String? = null, // Nullable for non-transfer transactions
    var targetGoalName: String? = null, // Nullable for non-transfer transactions
    var targetUserEmail: String? = null,    // Nullable, only needed for external transfers
    var amount: Double? = 0.0,            // Store transaction amount
    // Previously, createdAt was declared as Long:
    // val createdAt: Long = 0L
    // Change it to Instant:
//    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
//    val timestamp: Instant? = null,
    @ServerTimestamp
    val timestamp: Date? = null,
    var currency: String? = "USD"
) {
    @Exclude
    fun getTheCurrency(): CurrencyType {
        return currency?.let { CurrencyType.find(it) } ?: CurrencyType.USD
    }

//    fun getTransactionType(): TransactionType {
//        return TransactionType.valueOf(type)
//    }
//
//    fun getAmountFormatted(symbol: Boolean = true): String {
//        return if (symbol) {
//            "${getCurrency()}${"%,.2f".format(amount)}"
//        } else {
//            "$currency $amount" // Fallback if currency is not found
//        }
//    }
}