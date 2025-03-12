package com.bethwelamkenya.personalfinancetrackerspring

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import java.io.IOException

@Configuration
class FirebaseConfig {
    @PostConstruct
    fun initialize() {
        try {
            // Loads the file from the classpath (src/main/resources)
            val serviceAccountStream = this::class.java.getResourceAsStream("/my-banking-project-62e2a-firebase-adminsdk-fbsvc-1d4be59cd5.json")
                ?: throw IllegalStateException("Firebase service account JSON not found in classpath")

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStream)) // For Cloud Firestore, you can also set the database URL
                .setDatabaseUrl("https://my-banking-project-62e2a.firebaseio.com")
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

