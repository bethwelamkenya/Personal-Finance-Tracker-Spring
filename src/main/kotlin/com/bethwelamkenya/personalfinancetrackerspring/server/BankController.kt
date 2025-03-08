package com.bethwelamkenya.personalfinancetrackerspring.server

import com.bethwelamkenya.personalfinancetrackerspring.domain.BankAccount
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["http://localhost:3000"])
@RestController
@RequestMapping("/bank_accounts")
class BankController(private val bankService: BankService) {

    @PostMapping("/{id}")
    fun createBankAccount(@PathVariable id: String, @RequestBody accountDTO: BankAccount): ResponseEntity<BankAccount> {
        val bankAccount = bankService.createBankAccount(id, accountDTO)
        return ResponseEntity.ok(bankAccount)
    }

    @PutMapping("/{id}")
    fun updateBankAccount(@PathVariable id: String, @RequestBody updateData: BankAccount): ResponseEntity<BankAccount> {
        val updatedBankAccount = bankService.updateBankAccount(id, updateData)
        return ResponseEntity.ok(updatedBankAccount)
    }

    @GetMapping("/id/{id}")
    fun getBankAccount(@PathVariable id: String): ResponseEntity<BankAccount> {
        val user = bankService.getBankAccountById(id)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/number/{accountNumber}")
    fun getBankAccountByAccountNumber(@PathVariable accountNumber: String): ResponseEntity<BankAccount> {
        val user = bankService.getBankAccountByAccountNumber(accountNumber)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/email/{userEmail}")
    fun getBankAccounts(@PathVariable userEmail: String): ResponseEntity<List<BankAccount>> {
        val user = bankService.getAllAccounts(userEmail)
        return ResponseEntity.ok(user)
    }

    @DeleteMapping("/{id}")
    fun deleteBankAccount(@PathVariable id: String): ResponseEntity<Unit> {
        bankService.deleteAccount(id)
        return ResponseEntity.noContent().build()
    }
}
