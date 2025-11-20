package com.nrd.annonceservice.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String base64Key) {
        try {
            // MÊME CODE QUE USER-SERVICE !
            byte[] decodedKey = Base64.getDecoder().decode(base64Key);
            this.secretKey = Keys.hmacShaKeyFor(decodedKey);

            logger.info("JwtUtil initialisé avec clé de {} bits", secretKey.getEncoded().length * 8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Erreur d'initialisation JWT: " + e.getMessage(), e);
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }
    
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            logger.warn("Token JWT invalide: {}", e.getMessage());
            return false;
        }
    }

    // Méthode de debug
    public void debugToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            logger.info("✅ Token valide - User ID: {}", claims.getSubject());
        } catch (Exception e) {
            logger.error("❌ Token invalide: {}", e.getMessage());
        }
    }
}