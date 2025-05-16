package com.usuarios.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./") // asegura que lea desde la raíz del proyecto
                    .filename(".env")
                    .load();

            String privateKeyRaw = dotenv.get("FIREBASE_PRIVATE_KEY");
            String clientEmail = dotenv.get("FIREBASE_CLIENT_EMAIL");

            if (privateKeyRaw == null || clientEmail == null) {
                throw new IllegalStateException(
                        "❌ Variables de entorno de Firebase no están configuradas correctamente.");
            }

            System.out.println("🔎 FIREBASE_CLIENT_EMAIL: " + clientEmail);
            System.out.println("🔎 FIREBASE_PRIVATE_KEY starts with: " + privateKeyRaw.substring(0, 30));

            String privateKey = privateKeyRaw.replace("\\n", "\n");

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
                    dotenv.get("FIREBASE_TYPE"),
                    dotenv.get("FIREBASE_PROJECT_ID"),
                    dotenv.get("FIREBASE_PRIVATE_KEY_ID"),
                    privateKey,
                    clientEmail,
                    dotenv.get("FIREBASE_CLIENT_ID"),
                    dotenv.get("FIREBASE_AUTH_URI"),
                    dotenv.get("FIREBASE_TOKEN_URI"),
                    dotenv.get("FIREBASE_AUTH_PROVIDER_X509_CERT_URL"),
                    dotenv.get("FIREBASE_CLIENT_X509_CERT_URL"),
                    dotenv.get("FIREBASE_UNIVERSE_DOMAIN"));

            ByteArrayInputStream serviceAccount = new ByteArrayInputStream(
                    firebaseConfig.getBytes(StandardCharsets.UTF_8));

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase initialized successfully from environment variables.");
            } else {
                System.out.println("⚠️ Firebase already initialized, skipping initialization.");
            }

        } catch (Exception e) {
            System.err.println("❌ Error initializing Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }
}
