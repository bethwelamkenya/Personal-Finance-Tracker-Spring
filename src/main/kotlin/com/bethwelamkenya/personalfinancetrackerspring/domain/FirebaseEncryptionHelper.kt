package com.bethwelamkenya.personalfinancetrackerspring.domain

import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory

@Component
class FirebaseEncryptionHelper {

    private val AES_MODE = "AES/GCM/NoPadding"
    private val IV_SIZE = 12 // Recommended IV size for GCM
    private val TAG_LENGTH = 128 // Authentication tag length

    // Password hashing settings
    private val algorithm = "PBKDF2WithHmacSHA256"
    private val iterations = 10000
    private val keyLength = 256

    // 🔑 Fixed Secret Key (Derived from SHA-256 of "xZK9Gr0l1uu45euMSl5W+A==")
    private val secretKey: SecretKey by lazy {
        val keyString = "xZK9Gr0l1uu45euMSl5W+A=="
        val digest = MessageDigest.getInstance("SHA-256")
        val key = digest.digest(keyString.toByteArray())
        SecretKeySpec(key, "AES")
    }

    /**
     * 🔐 Hash a string into a Firebase-safe document ID using SHA-256.
     */
    fun hashForFirebase(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray())

        // Encode in Firebase-safe Base64 (URL-safe, no padding)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
    }

    /**
     * 🔒 Encrypts a string using AES-GCM (Firebase-safe output).
     */
    fun encryptForFirebase(plainText: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
        val gcmSpec = GCMParameterSpec(TAG_LENGTH, iv)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Store IV + Encrypted Data together
        val encryptedData = ByteBuffer.allocate(IV_SIZE + encrypted.size)
            .put(iv)
            .put(encrypted)
            .array()

        // Encode in Firebase-safe Base64
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedData)
    }

    /**
     * 🔓 Decrypts a Firebase-safe AES-GCM encrypted string.
     */
    fun decryptFromFirebase(encryptedData: String): String {
        val decodedData = Base64.getUrlDecoder().decode(encryptedData)
        val iv = decodedData.copyOfRange(0, IV_SIZE)
        val cipherText = decodedData.copyOfRange(IV_SIZE, decodedData.size)

        val cipher = Cipher.getInstance(AES_MODE)
        val gcmSpec = GCMParameterSpec(TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        return String(cipher.doFinal(cipherText))
    }

    /**
     * 🔑 Generates a secure salt for password hashing.
     */
    fun generateSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(salt)
    }

    /**
     * 🔑 Hashes a password with the given salt using PBKDF2.
     */
    fun hashPassword(password: String, salt: String): String {
        val saltBytes = Base64.getUrlDecoder().decode(salt)
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, iterations, keyLength)
        val factory = SecretKeyFactory.getInstance(algorithm)
        val hash = factory.generateSecret(spec).encoded
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
    }
}
