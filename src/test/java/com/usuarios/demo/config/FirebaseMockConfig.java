package com.usuarios.demo.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.*;
import com.google.auth.oauth2.GoogleCredentials;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@TestConfiguration
@Profile("test")
public class FirebaseMockConfig {

    @PostConstruct
    public void initFirebaseApp() {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(Mockito.mock(GoogleCredentials.class))
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }

    @Bean
    @Primary
    public FirebaseAuth firebaseAuth() throws Exception {
        FirebaseAuth mockAuth = Mockito.mock(FirebaseAuth.class);

        String uid = "uid123";

        // Mocked user record
        UserRecord userRecord = Mockito.mock(UserRecord.class);
        Mockito.when(userRecord.getUid()).thenReturn(uid);
        Mockito.when(userRecord.getEmail()).thenReturn("testuser@example.com");
        Mockito.when(userRecord.getDisplayName()).thenReturn("Test User");
        Mockito.when(userRecord.getCustomClaims()).thenReturn(Map.of("rol", "medico"));

        // FirebaseAuth behavior
        Mockito.when(mockAuth.getUserByEmail(Mockito.anyString()))
                .thenThrow(new RuntimeException("User not found"));
        Mockito.when(mockAuth.getUser(uid)).thenReturn(userRecord);
        Mockito.when(mockAuth.createUser(Mockito.any(UserRecord.CreateRequest.class)))
                .thenReturn(userRecord);
        Mockito.doNothing().when(mockAuth).setCustomUserClaims(Mockito.eq(uid), Mockito.anyMap());
        Mockito.doNothing().when(mockAuth).deleteUser(uid);
        Mockito.when(mockAuth.updateUser(Mockito.any(UserRecord.UpdateRequest.class)))
                .thenReturn(userRecord);

        // Exported user mock
        ExportedUserRecord exportedUser = Mockito.mock(ExportedUserRecord.class);
        UserMetadata metadata = Mockito.mock(UserMetadata.class);
        Mockito.when(exportedUser.getUid()).thenReturn(uid);
        Mockito.when(exportedUser.getEmail()).thenReturn("medico@kala.com");
        Mockito.when(exportedUser.getCustomClaims()).thenReturn(Map.of("rol", "medico"));
        Mockito.when(exportedUser.getDisplayName()).thenReturn("Dr. House");
        Mockito.when(exportedUser.getPhoneNumber()).thenReturn("+1234567890");
        Mockito.when(exportedUser.isDisabled()).thenReturn(false);
        Mockito.when(exportedUser.isEmailVerified()).thenReturn(true);
        Mockito.when(exportedUser.getUserMetadata()).thenReturn(metadata);
        Mockito.when(metadata.getCreationTimestamp()).thenReturn(123456789L);
        Mockito.when(metadata.getLastSignInTimestamp()).thenReturn(987654321L);

        ListUsersPage mockPage = Mockito.mock(ListUsersPage.class);
        Mockito.when(mockPage.iterateAll()).thenReturn(List.of(exportedUser));
        Mockito.when(mockAuth.listUsers(null)).thenReturn(mockPage);

        return mockAuth;
    }
}
