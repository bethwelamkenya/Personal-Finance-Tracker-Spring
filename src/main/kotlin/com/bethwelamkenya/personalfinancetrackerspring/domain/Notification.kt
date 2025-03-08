package com.bethwelamkenya.personalfinancetrackerspring.domain

data class Notification(
    val id: Int = 0,
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
