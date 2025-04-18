package services;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

@Service
public class JwtService {

    // Tu ID de proyecto Firebase
    private static final String FIREBASE_PROJECT_ID = "tesis-5b568";

    // Debe coincidir exactamente con el issuer de los tokens de Firebase
    private static final String ISSUER = "https://securetoken.google.com/" + FIREBASE_PROJECT_ID;

    /**
     * Extrae el correo electrónico del usuario desde el token JWT de Firebase.
     */
    public String extractUsername(String token) {
        try {
            return extractAllClaims(token).getStringClaim("email");
        } catch (Exception e) {
            throw new RuntimeException("❌ Token inválido o no verificable: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica la firma del token y extrae todos los claims.
     */
    public JWTClaimsSet extractAllClaims(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);

        // 🔐 Descargar claves públicas desde Firebase
        URL url = new URL("https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com");
        JWKSet publicKeys = JWKSet.load(url);

        // Buscar la clave pública correcta por el 'kid' del token
        String kid = signedJWT.getHeader().getKeyID();
        JWK jwk = publicKeys.getKeyByKeyId(kid);
        if (jwk == null) throw new SecurityException("❌ Clave pública no encontrada para el kid: " + kid);

        RSAPublicKey publicKey = jwk.toRSAKey().toRSAPublicKey();
        JWSVerifier verifier = new RSASSAVerifier(publicKey);

        // Validar firma del token
        if (!signedJWT.verify(verifier)) {
            throw new SecurityException("❌ Firma JWT inválida");
        }

        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

        // Validar issuer
        if (!ISSUER.equals(claims.getIssuer())) {
            throw new SecurityException("❌ Issuer inválido: " + claims.getIssuer());
        }

        // Validar expiración
        if (claims.getExpirationTime() == null || claims.getExpirationTime().before(new Date())) {
            throw new SecurityException("❌ Token expirado");
        }

        return claims;
    }
}
