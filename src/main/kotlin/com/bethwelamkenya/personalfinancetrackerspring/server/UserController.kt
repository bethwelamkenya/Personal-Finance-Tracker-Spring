package com.bethwelamkenya.personalfinancetrackerspring.server

import com.bethwelamkenya.personalfinancetrackerspring.domain.User
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["http://localhost:3000"])
@RestController
@RequestMapping("/users")
class UserController(private val firebaseService: FirebaseService) {
    @PostMapping("/signup")
    fun createUser(@RequestBody userDTO: User): ResponseEntity<User> {
        val user = firebaseService.createUser(userDTO)
        return ResponseEntity.ok(user)
    }

    @PostMapping("/login")
    fun logInUser(@RequestBody userDTO: User): ResponseEntity<User> {
        val user = firebaseService.logInUser(userDTO)
        return ResponseEntity.ok(user)
    }

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: String, @RequestBody updateData: User): ResponseEntity<User> {
        val updatedUser = firebaseService.updateUser(id, updateData)
        return ResponseEntity.ok(updatedUser)
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): ResponseEntity<User> {
        val user = firebaseService.getUserById(id)
        return ResponseEntity.ok(user)
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: String): ResponseEntity<Unit> {
        firebaseService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }

}
