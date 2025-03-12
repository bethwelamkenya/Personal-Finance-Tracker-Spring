package com.bethwelamkenya.personalfinancetrackerspring

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
class FirebaseConfig {
    @PostConstruct
    fun initialize() {
        try {
            // Provide the path to your service account JSON file.
            val serviceAccount =
                FileInputStream("../../../my-banking-project-62e2a-firebase-adminsdk-fbsvc-1d4be59cd5.json")

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount)) // For Cloud Firestore, you can also set the database URL
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

