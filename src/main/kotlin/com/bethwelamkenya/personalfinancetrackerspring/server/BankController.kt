package com.bethwelamkenya.personalfinancetrackerspring.server

import com.bethwelamkenya.personalfinancetrackerspring.domain.BankAccount
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["http://localhost:3000", "https://bekapersonalfinancetracker.netlify.app"])
@RestController
@RequestMapping("/bank_accounts")
class BankController(private val firebaseService: FirebaseService) {

    @PostMapping("/{id}")
    fun createBankAccount(@PathVariable id: String, @RequestBody accountDTO: BankAccount): ResponseEntity<BankAccount> {
        val bankAccount = firebaseService.createBankAccount(id, accountDTO)
        return ResponseEntity.ok(bankAccount)
    }

    @PutMapping("/{id}")
    fun updateBankAccount(@PathVariable id: String, @RequestBody updateData: BankAccount): ResponseEntity<BankAccount> {
        val updatedBankAccount = firebaseService.updateBankAccount(id, updateData)
        return ResponseEntity.ok(updatedBankAccount)
    }

    @GetMapping("/email={email}&id={id}")
    fun getBankAccount(@PathVariable email: String, @PathVariable id: String): ResponseEntity<BankAccount> {
        val user = firebaseService.getBankAccountById(email, id)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/number/email={email}&accountNumber={accountNumber}")
    fun getBankAccountByAccountNumber(@PathVariable email: String, @PathVariable accountNumber: String): ResponseEntity<BankAccount> {
        val user = firebaseService.getBankAccountByAccountNumber(email, accountNumber)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/email/{userEmail}")
    fun getBankAccounts(@PathVariable userEmail: String): ResponseEntity<List<BankAccount>> {
        val user = firebaseService.getAllAccounts(userEmail)
        return ResponseEntity.ok(user)
    }

    @DeleteMapping("/email={email}&id={id}")
    fun deleteBankAccount(@PathVariable email: String, @PathVariable id: String): ResponseEntity<Unit> {
        firebaseService.deleteAccount(email, id)
        return ResponseEntity.noContent().build()
    }
}
