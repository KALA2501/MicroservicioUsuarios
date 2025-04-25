package com.usuarios.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            String privateKey = System.getenv("FIREBASE_PRIVATE_KEY").replace("\\n", "\n");

            String firebaseConfig = String.format("""
                    {
                      "type": "%s",
                      "project_id": "%s",
                      "private_key_id": "%s",
                      "private_key": "%s",
                      "client_email": "%s",
                      "client_id": "%s",
                      "auth_uri": "%s",
                      "token_uri": "%s",
                      "auth_provider_x509_cert_url": "%s",
                      "client_x509_cert_url": "%s",
                      "universe_domain": "%s"
                    }
                    """,
                    System.getenv("FIREBASE_TYPE"),
                    System.getenv("FIREBASE_PROJECT_ID"),
                    System.getenv("FIREBASE_PRIVATE_KEY_ID"),
                    privateKey,
                    System.getenv("FIREBASE_CLIENT_EMAIL"),
                    System.getenv("FIREBASE_CLIENT_ID"),
                    System.getenv("FIREBASE_AUTH_URI"),
                    System.getenv("FIREBASE_TOKEN_URI"),
                    System.getenv("FIREBASE_AUTH_PROVIDER_X509_CERT_URL"),
                    System.getenv("FIREBASE_CLIENT_X509_CERT_URL"),
                    System.getenv("FIREBASE_UNIVERSE_DOMAIN"));

            ByteArrayInputStream serviceAccount = new ByteArrayInputStream(
                    firebaseConfig.getBytes(StandardCharsets.UTF_8));

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase initialized successfully from environment variables.");
            }

        } catch (Exception e) {
            System.err.println("❌ Error initializing Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
