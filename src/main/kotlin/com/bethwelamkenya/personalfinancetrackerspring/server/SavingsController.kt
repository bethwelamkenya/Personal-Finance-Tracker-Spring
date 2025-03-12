package com.bethwelamkenya.personalfinancetrackerspring.server

import com.bethwelamkenya.personalfinancetrackerspring.domain.SavingsGoal
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["http://localhost:3000", "https://bekapersonalfinancetracker.netlify.app"])
@RestController
@RequestMapping("/savings_goals")
class SavingsController(private val firebaseService: FirebaseService) {

    @PostMapping("/{id}")
    fun createSavingsGoal(@PathVariable id: String, @RequestBody accountDTO: SavingsGoal): ResponseEntity<SavingsGoal> {
        val bankAccount = firebaseService.createSavingsGoal(id, accountDTO)
        return ResponseEntity.ok(bankAccount)
    }

    @PutMapping("/{id}")
    fun updateSavingsGoal(@PathVariable id: String, @RequestBody updateData: SavingsGoal): ResponseEntity<SavingsGoal> {
        val updatedBankAccount = firebaseService.updateSavingsGoal(id, updateData)
        return ResponseEntity.ok(updatedBankAccount)
    }

    @GetMapping("/id/email={email}&id={id}")
    fun getSavingsGoal(@PathVariable email: String, @PathVariable id: String): ResponseEntity<SavingsGoal> {
        val user = firebaseService.getSavingsGoalById(email, id)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/name/email={email}&goalName={goalName}")
    fun getSavingsGoalsByGoalName(
        @PathVariable email: String,
        @PathVariable goalName: String
    ): ResponseEntity<SavingsGoal> {
        val user = firebaseService.getSavingsGoalByGoalName(email, goalName)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/email/{userEmail}")
    fun getSavingsGoals(@PathVariable userEmail: String): ResponseEntity<List<SavingsGoal>> {
        val user = firebaseService.getAllSavings(userEmail)
        return ResponseEntity.ok(user)
    }

    @DeleteMapping("/email={email}&id={id}")
    fun deleteSavings(@PathVariable email: String, @PathVariable id: String): ResponseEntity<Unit> {
        firebaseService.deleteSaving(email, id)
        return ResponseEntity.noContent().build()
    }
}
