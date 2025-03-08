package com.bethwelamkenya.personalfinancetrackerspring.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    var id: String = "",

    @Column(name = "name", nullable = true)
    var name: String? = "",

    @Column(name = "email", nullable = false)
    var email: String? = "",

    @Column(name = "passwordHash", nullable = false) // 🔥 Explicit column mapping
    var passwordHash: String? = "",

    @Column(name = "salt", nullable = false) // 🔥 Explicit column mapping
    var salt: String? = "",

    @Column(name = "createdAt", insertable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

