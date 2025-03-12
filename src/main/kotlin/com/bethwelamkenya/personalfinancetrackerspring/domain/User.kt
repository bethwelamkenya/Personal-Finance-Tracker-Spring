package com.bethwelamkenya.personalfinancetrackerspring.domain

import com.google.cloud.firestore.annotation.ServerTimestamp
import java.util.*

data class User(
    var id: String? = "",
    var name: String? = "",
    var email: String? = "",
    var passwordHash: String? = "",
    var salt: String? = "",
    // Previously, createdAt was declared as Long:
    // val createdAt: Long = 0L
    // Change it to Instant:
//    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
//    val createdAt: Instant? = null
    @ServerTimestamp
    val createdAt: Date? = null
)

