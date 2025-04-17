package services;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

@Service
public class JwtService {

    private static final String FIREBASE_PROJECT_ID = "tesis-5b568";
    private static final String ISSUER = "https://securetoken.google.com/" + FIREBASE_PROJECT_ID;

    public String extractUsername(String token) {
        try {
            return extractAllClaims(token).getStringClaim("email");
        } catch (Exception e) {
            throw new RuntimeException("❌ Token inválido o no verificable: " + e.getMessage(), e);
        }
    }

    public JWTClaimsSet extractAllClaims(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);

        // 🔐 Descarga de claves públicas desde Firebase
        URL url = new URL("https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com");
        JWKSet publicKeys = JWKSet.load(url);

        // Obtiene la clave por 'kid' (key ID) del token
        String kid = signedJWT.getHeader().getKeyID();
        JWK jwk = publicKeys.getKeyByKeyId(kid);
        if (jwk == null) throw new SecurityException("❌ Clave pública no encontrada para el kid: " + kid);

        RSAPublicKey publicKey = jwk.toRSAKey().toRSAPublicKey();
        JWSVerifier verifier = new RSASSAVerifier(publicKey);

        if (!signedJWT.verify(verifier)) {
            throw new SecurityException("❌ Firma JWT inválida");
        }

        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

        if (!ISSUER.equals(claims.getIssuer())) {
            throw new SecurityException("❌ Issuer inválido: " + claims.getIssuer());
        }

        return claims;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = extractUsername(token);
            return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token).getExpirationTime().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
// 🔑 Este servicio se encarga de validar y extraer información del token JWT emitido por Firebase.