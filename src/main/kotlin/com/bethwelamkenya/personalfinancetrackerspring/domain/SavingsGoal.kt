package com.bethwelamkenya.personalfinancetrackerspring.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "savings_goals")
data class SavingsGoal(
    @Id
    var id: String = "",

    @Column(name = "accountNumber", nullable = false)
    var accountNumber: String? = "",

    @Column(name = "goalName", nullable = false)
    var goalName: String? = "",

    @Column(name = "targetAmount", nullable = false)
    var targetAmount: Double? = 0.0,

    @Column(name = "savedAmount", nullable = false)
    var savedAmount: Double? = 0.0,

    @Column(name = "currency", nullable = false)
    var currency: String? = "USD",

    @Column(name = "userEmail", nullable = false)
    var userEmail: String? = "",

    @Column(name = "createdAt", insertable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun getCurrency(): CurrencyType {
        return currency?.let { CurrencyType.find(it) } ?: CurrencyType.USD
    }
}

