package com.bethwelamkenya.personalfinancetrackerspring

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AppException::class)
    fun handleAppException(ex: AppException): ResponseEntity<Map<String, String>> {
        val status = when (ex) {
            is AppException.UserAlreadyExists -> HttpStatus.CONFLICT
            is AppException.BankAccountAlreadyExists -> HttpStatus.CONFLICT
            is AppException.SavingsGoalAlreadyExists -> HttpStatus.CONFLICT
            is AppException.UserNotFound -> HttpStatus.NOT_FOUND
            is AppException.BankAccountNotFound -> HttpStatus.NOT_FOUND
            is AppException.TransactionNotFound -> HttpStatus.NOT_FOUND
            is AppException.SavingsGoalNotFound -> HttpStatus.NOT_FOUND
            is AppException.InsufficientDetails -> HttpStatus.BAD_REQUEST
            is AppException.InvalidRequest -> HttpStatus.BAD_REQUEST
            is AppException.InvalidPassword -> HttpStatus.NOT_FOUND
            is AppException.InsufficientFunds -> HttpStatus.METHOD_NOT_ALLOWED
            is AppException.InvalidTransactionType -> HttpStatus.BAD_REQUEST
            is AppException.UnknownError -> HttpStatus.PROCESSING
        }
//        println("the problem: ${ex.message}")
        return ResponseEntity.status(status).body(mapOf("error" to ex.message!!))
    }
}

