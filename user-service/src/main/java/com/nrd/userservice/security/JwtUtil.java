package com.nrd.userservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
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
    private final long expiration;

    public JwtUtil(@Value("${jwt.secret:}") String base64Key,
                   @Value("${jwt.expiration:3600000}") long expiration) {

        if (base64Key == null || base64Key.trim().isEmpty()) {
            // Génération automatique d'une clé sécurisée
            SecretKey generatedKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            this.secretKey = generatedKey;
            String autoKey = Base64.getEncoder().encodeToString(generatedKey.getEncoded());
            logger.warn("Aucune clé JWT configurée. Clé auto-générée : {}", autoKey);
        } else {
            try {
                byte[] decodedKey = Base64.getDecoder().decode(base64Key);
                this.secretKey = Keys.hmacShaKeyFor(decodedKey);

                // Vérification de la taille de la clé
                if (secretKey.getEncoded().length < 64) { // 64 bytes = 512 bits
                    throw new IllegalArgumentException("Clé JWT trop courte. Minimum 512 bits requis.");
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Format de clé JWT invalide", e);
            }
        }
        this.expiration = expiration;
    }

    public String generateToken(Long userId, String role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
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
       //     logger.warn("Token JWT invalide: {}", e.getMessage());
            return false;
        }
    }
}