package com.bethwelamkenya.personalfinancetrackerspring.server

import com.bethwelamkenya.personalfinancetrackerspring.domain.Transaction
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["http://localhost:3000"])
@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val transactionService: TransactionService,
    private val bankService: BankService,
    private val savingsService: SavingsService
) {

    @PostMapping("/create/{id}")
    fun createTransaction(@PathVariable id: String, @RequestBody transactionDTO: Transaction): ResponseEntity<Transaction> {
        println("Received request to create transaction for id: $id")
        println("Transaction data received: $transactionDTO")
        val transaction = transactionService.createTransaction(id, transactionDTO, bankService, savingsService)
        return ResponseEntity.ok(transaction)
    }

    @GetMapping("/id/{id}")
    fun getTransaction(@PathVariable id: Long): ResponseEntity<Transaction> {
        val transaction = transactionService.getTransactionById(id)
        return ResponseEntity.ok(transaction)
    }

    @GetMapping("/email/{userEmail}")
    fun getTransactions(@PathVariable userEmail: String): ResponseEntity<List<Transaction>> {
        val transactions = transactionService.getAllTransactions(userEmail)
        return ResponseEntity.ok(transactions)
    }
}
