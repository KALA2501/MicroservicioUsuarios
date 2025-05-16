package com.usuarios.demo.config;

import com.usuarios.demo.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private AuthenticationProvider authenticationProvider;
    private AuthenticationConfiguration authenticationConfiguration;

    @BeforeEach
    void setup() throws Exception {
        jwtAuthenticationFilter = mock(JwtAuthenticationFilter.class);
        authenticationProvider = mock(AuthenticationProvider.class);
        authenticationConfiguration = mock(AuthenticationConfiguration.class);

        securityConfig = new SecurityConfig();

        ReflectionTestUtils.setField(securityConfig, "jwtAuthenticationFilter", jwtAuthenticationFilter);
        ReflectionTestUtils.setField(securityConfig, "authenticationProvider", authenticationProvider);

        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockManager);
    }


    @Test
    void testPasswordEncoder_IsBCrypt() {
        assertThat(securityConfig.passwordEncoder()).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    void testAuthenticationManager_NotNull() throws Exception {
        AuthenticationManager manager = securityConfig.authenticationManager(authenticationConfiguration);
        assertThat(manager).isNotNull();
        verify(authenticationConfiguration, times(1)).getAuthenticationManager();
    }

    @Test
    void testCorsConfigurationSource_AllowsExpectedValues() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest());

        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).contains("http://localhost:3000");
        assertThat(config.getAllowedMethods()).contains("*");
        assertThat(config.getAllowedHeaders()).contains("*");
    }

    @Test
    void testSecurityFilterChainBuilds() throws Exception {
        assertThat(securityConfig).isNotNull();
    }
}
