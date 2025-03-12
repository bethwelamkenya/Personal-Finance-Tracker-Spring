package com.bethwelamkenya.personalfinancetrackerspring.server

import com.bethwelamkenya.personalfinancetrackerspring.domain.Transaction
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["http://localhost:3000", "https://bekapersonalfinancetracker.netlify.app"])
@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val firebaseService: FirebaseService
) {

    @PostMapping("/create/{id}")
    fun createTransaction(@PathVariable id: String, @RequestBody transactionDTO: Transaction): ResponseEntity<Transaction> {
        println("Received request to create transaction for id: $id")
        println("Transaction data received: $transactionDTO")
        val transaction = firebaseService.createTransaction(id, transactionDTO)
        return ResponseEntity.ok(transaction)
    }

    @GetMapping("/id/email={email}&id={id}")
    fun getTransaction(@PathVariable email: String, @PathVariable id: String): ResponseEntity<Transaction> {
        val transaction = firebaseService.getTransactionById(email, id)
        return ResponseEntity.ok(transaction)
    }

    @GetMapping("/email/{userEmail}")
    fun getTransactions(@PathVariable userEmail: String): ResponseEntity<List<Transaction>> {
        val transactions = firebaseService.getAllTransactions(userEmail)
        return ResponseEntity.ok(transactions)
    }
}
