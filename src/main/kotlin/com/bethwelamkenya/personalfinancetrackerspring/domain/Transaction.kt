package com.bethwelamkenya.personalfinancetrackerspring.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "transactions")
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ✅ Auto-generate ID
    var id: Long = 0,

    @Column(name = "type", nullable = false)
    var type: String? = TransactionType.DEPOSIT.name, // Store enum as a string

    @Column(name = "accountNumber", nullable = false)
    var accountNumber: String? = "",

    @Column(name = "userEmail", nullable = false)
    var userEmail: String? = "",

    @Column(name = "goalName", nullable = false)
    var goalName: String? = "",

    @Column(name = "targetAccountNumber", nullable = true)
    var targetAccountNumber: String? = null, // Nullable for non-transfer transactions

    @Column(name = "targetGoalName", nullable = true)
    var targetGoalName: String? = null, // Nullable for non-transfer transactions

    @Column(name = "targetUserEmail", nullable = true)
    var targetUserEmail: String? = null,    // Nullable, only needed for external transfers

    @Column(name = "amount", nullable = false)
    var amount: Double? = 0.0,            // Store transaction amount

    @Column(name = "timestamp", insertable = false, updatable = false)
    val timestamp: LocalDateTime = LocalDateTime.now(),

    @Column(name = "currency", nullable = false)
    var currency: String? = "USD"
) {
    fun getCurrency(): CurrencyType {
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