package com.usuarios.demo.services;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.JWSVerifier;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    private static final String FIREBASE_PROJECT_ID = "tesis-5b568";
    private static final String ISSUER = "https://securetoken.google.com/" + FIREBASE_PROJECT_ID;
    private static final String JWK_URL = "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";

    private JWKSet cachedPublicKeys = null;
    private long lastFetchedTime = 0;
    private static final long CACHE_EXPIRATION_TIME = TimeUnit.HOURS.toMillis(1); // Cache duration (1 hour)

    public String extractUsername(String token) {
        try {
            return extractAllClaims(token).getStringClaim("email");
        } catch (Exception e) {
            throw new RuntimeException("❌ Token inválido o no verificable: " + e.getMessage(), e);
        }
    }

    public JWTClaimsSet extractAllClaims(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);

        // Check if cached keys are valid
        if (System.currentTimeMillis() - lastFetchedTime > CACHE_EXPIRATION_TIME || cachedPublicKeys == null) {
            fetchAndCachePublicKeys();
        }

        // Get the public key corresponding to the JWT's "kid"
        String kid = signedJWT.getHeader().getKeyID();
        JWK jwk = cachedPublicKeys.getKeyByKeyId(kid);
        if (jwk == null)
            throw new SecurityException("❌ Clave pública no encontrada para el kid: " + kid);

        RSAPublicKey publicKey = jwk.toRSAKey().toRSAPublicKey();
        JWSVerifier verifier = new RSASSAVerifier(publicKey);

        // Validate the signature of the JWT
        if (!signedJWT.verify(verifier)) {
            throw new SecurityException("❌ Firma JWT inválida");
        }

        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

        // Validate issuer
        if (!ISSUER.equals(claims.getIssuer())) {
            throw new SecurityException("❌ Issuer inválido: " + claims.getIssuer());
        }

        // Validate expiration
        if (claims.getExpirationTime() == null || claims.getExpirationTime().before(new Date())) {
            throw new SecurityException("❌ Token expirado");
        }

        return claims;
    }

    // Fetch the public keys from Firebase and cache them
    private void fetchAndCachePublicKeys() throws Exception {
        URL url = new URL(JWK_URL);
        cachedPublicKeys = JWKSet.load(url);
        lastFetchedTime = System.currentTimeMillis();
    }
}
