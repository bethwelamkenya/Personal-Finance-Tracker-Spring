package com.bethwelamkenya.personalfinancetrackerspring.server

import com.bethwelamkenya.personalfinancetrackerspring.domain.SavingsGoal
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["http://localhost:3000"])
@RestController
@RequestMapping("/savings_goals")
class SavingsController(private val savingsService: SavingsService) {

    @PostMapping("/{id}")
    fun createSavingsGoal(@PathVariable id: String, @RequestBody accountDTO: SavingsGoal): ResponseEntity<SavingsGoal> {
        val bankAccount = savingsService.createSavingsGoal(id, accountDTO)
        return ResponseEntity.ok(bankAccount)
    }

    @PutMapping("/{id}")
    fun updateSavingsGoal(@PathVariable id: String, @RequestBody updateData: SavingsGoal): ResponseEntity<SavingsGoal> {
        val updatedBankAccount = savingsService.updateSavingsGoal(id, updateData)
        return ResponseEntity.ok(updatedBankAccount)
    }

    @GetMapping("/id/{id}")
    fun getSavingsGoal(@PathVariable id: String): ResponseEntity<SavingsGoal> {
        val user = savingsService.getSavingsGoalById(id)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/name/{goalName}")
    fun getSavingsGoalsByGoalName(@PathVariable goalName: String): ResponseEntity<SavingsGoal> {
        val user = savingsService.getSavingsGoalByGoalName(goalName)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/email/{userEmail}")
    fun getSavingsGoals(@PathVariable userEmail: String): ResponseEntity<List<SavingsGoal>> {
        val user = savingsService.getAllSavings(userEmail)
        return ResponseEntity.ok(user)
    }

    @DeleteMapping("/{id}")
    fun deleteSavings(@PathVariable id: String): ResponseEntity<Unit> {
        savingsService.deleteSaving(id)
        return ResponseEntity.noContent().build()
    }
}
