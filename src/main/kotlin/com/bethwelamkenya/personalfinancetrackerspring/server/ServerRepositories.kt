package com.bethwelamkenya.personalfinancetrackerspring.server

import com.bethwelamkenya.personalfinancetrackerspring.domain.BankAccount
import com.bethwelamkenya.personalfinancetrackerspring.domain.SavingsGoal
import com.bethwelamkenya.personalfinancetrackerspring.domain.Transaction
import com.bethwelamkenya.personalfinancetrackerspring.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String>

@Repository
interface BanksRepository : JpaRepository<BankAccount, String>

@Repository
interface SavingsRepository : JpaRepository<SavingsGoal, String>

@Repository
interface TransactionsRepository : JpaRepository<Transaction, Long>