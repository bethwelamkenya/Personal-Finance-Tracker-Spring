package com.bethwelamkenya.personalfinancetrackerspring.server

import com.bethwelamkenya.personalfinancetrackerspring.AppException
import com.bethwelamkenya.personalfinancetrackerspring.domain.FirebaseEncryptionHelper
import com.bethwelamkenya.personalfinancetrackerspring.domain.User
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository, private val encryptionHelper: FirebaseEncryptionHelper) {
    fun createUser(user: User): User {
        if (user.email.isNullOrBlank() || user.name.isNullOrBlank() || user.passwordHash.isNullOrBlank()) {
            throw AppException.InsufficientDetails() // 🔥 Throw exception
        }

        val userId = user.email?.let { encryptionHelper.hashForFirebase(it) }

        // Check if user already exists
        if (userId == null || userRepository.existsById(userId)) {
            throw AppException.UserAlreadyExists()
        }

        val encryptedName = user.name?.let { encryptionHelper.encryptForFirebase(it) }
        val encryptedEmail = user.email?.let { encryptionHelper.encryptForFirebase(it) }
        val salt = encryptionHelper.generateSalt()
        val hashedPassword = user.passwordHash?.let { encryptionHelper.hashPassword(it, salt) }

        val savedUser =
            User(id = userId, name = encryptedName, email = encryptedEmail, passwordHash = hashedPassword, salt = salt)
        return userRepository.save(savedUser)
    }

    fun logInUser(user: User): User {
        val id = user.email?.let { encryptionHelper.hashForFirebase(it) } ?: throw AppException.InsufficientDetails()
        val savedUser = userRepository.findById(id).orElseThrow { AppException.UserNotFound() }
        val salt: String = savedUser.salt!!
        val passwordHash = user.passwordHash?.let { encryptionHelper.hashPassword(password = it, salt = salt) }

        println(passwordHash + " " + savedUser.passwordHash)
        if (passwordHash != savedUser.passwordHash) {
            println(passwordHash + " " + savedUser.passwordHash)
            throw AppException.InvalidPassword()
        }
        val decryptedUser = savedUser.copy(
            name = savedUser.name?.let { encryptionHelper.decryptFromFirebase(it) },
            email = savedUser.email?.let {
                encryptionHelper.decryptFromFirebase(it)
            })
        println(decryptedUser.toString())
        return decryptedUser
    }

    fun updateUser(id: String, user: User): User {
        val existingUser = userRepository.findById(id).orElseThrow { AppException.UserNotFound() }

        val newUserId = user.email?.let { encryptionHelper.hashForFirebase(it) } ?: existingUser.id
        val encryptedName = user.name?.let { encryptionHelper.encryptForFirebase(it) } ?: existingUser.name
        val encryptedEmail = user.email?.let { encryptionHelper.encryptForFirebase(it) } ?: existingUser.email
        val salt = user.passwordHash?.let { encryptionHelper.generateSalt() } ?: existingUser.salt
        val hashedPassword =
            user.passwordHash?.let { encryptionHelper.hashPassword(it, salt!!) } ?: existingUser.passwordHash

        // Update only non-null fields
        val updatedUser = existingUser.copy(
            id = newUserId,
            name = encryptedName,
            email = encryptedEmail,
            passwordHash = hashedPassword,
            salt = salt
        )
        return userRepository.save(updatedUser)
    }

    fun getUserById(id: String): User {
        val user = userRepository.findById(id).orElseThrow { AppException.UserNotFound() }
        return user.copy(
            name = user.name?.let { encryptionHelper.decryptFromFirebase(it) },
            email = user.email?.let {
                encryptionHelper.decryptFromFirebase(it)
            })
    }

    fun getUserByEmail(email: String): User {
        val userId = encryptionHelper.hashForFirebase(email)
        val user = userRepository.findById(userId).orElseThrow { AppException.UserNotFound() }
        return user.copy(
            name = user.name?.let { encryptionHelper.decryptFromFirebase(it) },
            email = user.email?.let {
                encryptionHelper.decryptFromFirebase(it)
            })
    }

    fun deleteUser(id: String) {
        val user = userRepository.findById(id).orElseThrow { AppException.UserNotFound() }
        userRepository.delete(user)
    }

}
