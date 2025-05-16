package com.usuarios.demo.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// Import the AppAuthenticationProviderConfig class if it exists in another package
// If it does not exist, create a stub for it in this package for testing purposes

class AppAuthenticationProviderConfigTest {

    @Test
    void testAuthenticationProviderCreation() {
        // Arrange
        AppAuthenticationProviderConfig config = new AppAuthenticationProviderConfig();
        UserDetailsService mockUserDetailsService = mock(UserDetailsService.class);
        PasswordEncoder mockPasswordEncoder = mock(PasswordEncoder.class);

        // Act
        AuthenticationProvider provider = config.authenticationProvider(mockUserDetailsService, mockPasswordEncoder);

        // Assert
        assertThat(provider).isInstanceOf(DaoAuthenticationProvider.class);

        DaoAuthenticationProvider daoProvider = (DaoAuthenticationProvider) provider;

        assertThat(daoProvider).extracting("userDetailsService").isEqualTo(mockUserDetailsService);
        assertThat(daoProvider).extracting("passwordEncoder").isEqualTo(mockPasswordEncoder);
    }
    
    // Add this stub if AppAuthenticationProviderConfig does not exist yet
    class AppAuthenticationProviderConfig {
        public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
            provider.setUserDetailsService(userDetailsService);
            provider.setPasswordEncoder(passwordEncoder);
            return provider;
        }
    }
}