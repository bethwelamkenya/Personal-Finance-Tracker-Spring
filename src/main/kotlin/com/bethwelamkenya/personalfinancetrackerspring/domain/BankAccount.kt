package com.bethwelamkenya.personalfinancetrackerspring.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "bank_accounts")
data class BankAccount(
    @Id
    val id: String = "",

    @Column(name = "accountNumber", nullable = false)
    var accountNumber: String? = "",

    @Column(name = "holderName", nullable = false)
    var holderName: String? = "",

    @Column(name = "bankName", nullable = false)
    var bankName: String? = "",

    @Column(name = "balance", nullable = false)
    var balance: Double? = 0.0,

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
