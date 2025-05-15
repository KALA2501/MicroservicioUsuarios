package com.usuarios.demo.services;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.*;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.security.interfaces.RSAPublicKey;
import com.nimbusds.jose.JWSAlgorithm;
import java.net.URL;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    private JwtService jwtService;

    private static MockedStatic<SignedJWT> signedJwtStatic;
    private static MockedStatic<JWKSet> jwkSetStatic;

    private SignedJWT signedJWT;
    private JWKSet jwkSet;
    private JWK jwk;
    private RSAPublicKey publicKey;
    private JWTClaimsSet claimsSet;

    @BeforeAll
    static void mockStaticInit() {
        signedJwtStatic = mockStatic(SignedJWT.class);
        jwkSetStatic = mockStatic(JWKSet.class);
    }

    @AfterAll
    static void mockStaticClose() {
        signedJwtStatic.close();
        jwkSetStatic.close();
    }

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        // Mocks
        signedJWT = mock(SignedJWT.class);
        jwkSet = mock(JWKSet.class);
        jwk = mock(JWK.class);
        publicKey = mock(RSAPublicKey.class);
        claimsSet = new JWTClaimsSet.Builder()
                .issuer("https://securetoken.google.com/tesis-5b568")
                .expirationTime(new Date(System.currentTimeMillis() + 60000)) // +1 min
                .claim("email", "usuario@kala.com")
                .claim("rol", "medico")
                .build();

        // Mock comportamiento
        signedJwtStatic.when(() -> SignedJWT.parse(anyString())).thenReturn(signedJWT);
        when(signedJWT.getHeader()).thenReturn(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("abc123").build());
        when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);

        // Verificación
        when(signedJWT.verify(any(JWSVerifier.class))).thenReturn(true);

        // JWKSet
        jwkSetStatic.when(() -> JWKSet.load(any(URL.class))).thenReturn(jwkSet);
        when(jwkSet.getKeyByKeyId("abc123")).thenReturn(jwk);

        // RSAKey
        RSAKey rsaKey = mock(RSAKey.class);
        when(jwk.toRSAKey()).thenReturn(rsaKey);
        when(rsaKey.toRSAPublicKey()).thenReturn(publicKey);
    }

    @Test
    void testExtractUsername() {
        String email = jwtService.extractUsername("fake.jwt.token");
        assertEquals("usuario@kala.com", email);
    }

    @Test
    void testExtractStringClaim() {
        String rol = jwtService.extractStringClaim("fake.jwt.token", "rol");
        assertEquals("medico", rol);
    }

    @Test
    void testExtractFirstAvailableClaim() {
        String valor = jwtService.extractFirstAvailableClaim("fake.jwt.token", "noExiste", "rol", "otra");
        assertEquals("medico", valor);
    }

    @Test
    void testDecodeToken() throws Exception {
        JWTClaimsSet set = jwtService.decodeToken("fake.jwt.token");
        assertEquals("usuario@kala.com", set.getStringClaim("email"));
    }
}
