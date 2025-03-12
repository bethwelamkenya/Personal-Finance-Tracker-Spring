package com.bethwelamkenya.personalfinancetrackerspring.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.google.cloud.firestore.annotation.Exclude
import com.google.cloud.firestore.annotation.ServerTimestamp
import java.time.Instant
import java.util.*

data class SavingsGoal(
    var id: String? = "",
    var accountNumber: String? = "",
    var goalName: String? = "",
    var targetAmount: Double? = 0.0,
    var savedAmount: Double? = 0.0,
    var currency: String? = "USD",
    // Previously, createdAt was declared as Long:
    // val createdAt: Long = 0L
    // Change it to Instant:
//    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
//    val createdAt: Instant? = null
    @ServerTimestamp
    val createdAt: Date? = null
) {
    @Exclude
    fun getTheCurrency(): CurrencyType {
        return currency?.let { CurrencyType.find(it) } ?: CurrencyType.USD
    }
}

