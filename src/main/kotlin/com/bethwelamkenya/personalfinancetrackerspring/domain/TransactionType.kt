package com.bethwelamkenya.personalfinancetrackerspring.domain

enum class TransactionType {
    DEPOSIT, WITHDRAW, TRANSFER_OUT, TRANSFER_OUT_TO,
    DEPOSIT_GOAL, WITHDRAW_GOAL, TRANSFER_GOAL_OUT,
    TRANSFER_GOAL_OUT_TO
}