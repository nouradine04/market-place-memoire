package com.nrd.messagerie.security;

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
            SecretKey generatedKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            this.secretKey = generatedKey;
            String autoKey = Base64.getEncoder().encodeToString(generatedKey.getEncoded());
            logger.warn("Aucune clé JWT configurée. Clé auto-générée : {}", autoKey);
        } else {
            try {
                byte[] decodedKey = Base64.getDecoder().decode(base64Key);
                this.secretKey = Keys.hmacShaKeyFor(decodedKey);

                if (secretKey.getEncoded().length < 64) {
                    throw new IllegalArgumentException("Clé JWT trop courte. Minimum 512 bits requis.");
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Format de clé JWT invalide", e);
            }
        }
        this.expiration = expiration;
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            String subject = claims.getSubject();
            logger.debug("🎯 Extracted user ID from token: {}", subject);
            
            return Long.parseLong(subject);
        } catch (Exception e) {
            logger.error("❌ Error extracting user ID from token: {}", e.getMessage());
            throw e;
        }
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
            logger.debug("🔐 Validating JWT token of length: {}", token.length());
            
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                    
            logger.debug("✅ Token valid for user: {}", claims.getSubject());
            
            // Vérifier l'expiration
            if (claims.getExpiration().before(new Date())) {
                logger.warn("❌ Token expired for user: {}", claims.getSubject());
                return false;
            }
            
            return true;
        } catch (SignatureException e) {
            logger.error("❌ Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.error("❌ Malformed JWT token: {}", e.getMessage());
            return false;
        } catch (ExpiredJwtException e) {
            logger.error("❌ JWT token expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            logger.error("❌ Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("❌ JWT claims string is empty: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("❌ JWT validation error: {}", e.getMessage(), e);
            return false;
        }
    }
}