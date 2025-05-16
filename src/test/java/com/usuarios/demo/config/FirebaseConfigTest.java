package com.usuarios.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FirebaseConfigTest {

    private FirebaseConfig firebaseConfig;

    @BeforeEach
    void setUp() {
        firebaseConfig = new FirebaseConfig();
    }

    @Test
    void testInitialize_SuccessfulInitialization() throws Exception {
        Dotenv mockDotenv = mock(Dotenv.class);
        DotenvBuilder builder = mock(DotenvBuilder.class);

        when(builder.directory("./")).thenReturn(builder);
        when(builder.filename(".env")).thenReturn(builder);
        when(builder.load()).thenReturn(mockDotenv);

        when(mockDotenv.get("FIREBASE_TYPE")).thenReturn("service_account");
        when(mockDotenv.get("FIREBASE_PROJECT_ID")).thenReturn("test-project");
        when(mockDotenv.get("FIREBASE_PRIVATE_KEY_ID")).thenReturn("somekeyid");
        when(mockDotenv.get("FIREBASE_PRIVATE_KEY")).thenReturn("-----BEGIN PRIVATE KEY-----\\nKEY\\n-----END PRIVATE KEY-----");
        when(mockDotenv.get("FIREBASE_CLIENT_EMAIL")).thenReturn("firebase@test.com");
        when(mockDotenv.get("FIREBASE_CLIENT_ID")).thenReturn("123456789");
        when(mockDotenv.get("FIREBASE_AUTH_URI")).thenReturn("https://auth");
        when(mockDotenv.get("FIREBASE_TOKEN_URI")).thenReturn("https://token");
        when(mockDotenv.get("FIREBASE_AUTH_PROVIDER_X509_CERT_URL")).thenReturn("https://provider.cert");
        when(mockDotenv.get("FIREBASE_CLIENT_X509_CERT_URL")).thenReturn("https://client.cert");
        when(mockDotenv.get("FIREBASE_UNIVERSE_DOMAIN")).thenReturn("googleapis.com");

        try (
            MockedStatic<Dotenv> dotenvStatic = mockStatic(Dotenv.class);
            MockedStatic<FirebaseApp> firebaseAppStatic = mockStatic(FirebaseApp.class);
            MockedStatic<GoogleCredentials> credentialsStatic = mockStatic(GoogleCredentials.class)
        ) {
            dotenvStatic.when(Dotenv::configure).thenReturn(builder);
            firebaseAppStatic.when(FirebaseApp::getApps).thenReturn(Collections.emptyList());

            GoogleCredentials mockCredentials = mock(GoogleCredentials.class);
            credentialsStatic.when(() -> GoogleCredentials.fromStream(any(ByteArrayInputStream.class)))
                             .thenReturn(mockCredentials);

            firebaseAppStatic.when(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)))
                             .thenReturn(mock(FirebaseApp.class));

            assertDoesNotThrow(() -> firebaseConfig.initialize());

            firebaseAppStatic.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)), times(1));
        }
    }

    @Test
    void testFirebaseAuthBeanReturnsInstance() {
        try (
            MockedStatic<FirebaseApp> appStatic = mockStatic(FirebaseApp.class);
            MockedStatic<FirebaseAuth> authStatic = mockStatic(FirebaseAuth.class)
        ) {
            FirebaseApp mockApp = mock(FirebaseApp.class);
            FirebaseAuth mockAuth = mock(FirebaseAuth.class);

            appStatic.when(FirebaseApp::getInstance).thenReturn(mockApp);
            authStatic.when(FirebaseAuth::getInstance).thenReturn(mockAuth);

            FirebaseAuth auth = firebaseConfig.firebaseAuth();
            assertNotNull(auth);
            assertEquals(mockAuth, auth);
        }
    }

    @Test
    void testInitialize_MissingEnvVariables_ShouldHandleGracefully() {
        Dotenv mockDotenv = mock(Dotenv.class);
        DotenvBuilder builder = mock(DotenvBuilder.class);

        when(builder.directory("./")).thenReturn(builder);
        when(builder.filename(".env")).thenReturn(builder);
        when(builder.load()).thenReturn(mockDotenv);

        when(mockDotenv.get("FIREBASE_PRIVATE_KEY")).thenReturn(null);
        when(mockDotenv.get("FIREBASE_CLIENT_EMAIL")).thenReturn(null);

        try (
            MockedStatic<Dotenv> dotenvStatic = mockStatic(Dotenv.class)
        ) {
            dotenvStatic.when(Dotenv::configure).thenReturn(builder);

            assertDoesNotThrow(() -> firebaseConfig.initialize());
        }
    }
}
