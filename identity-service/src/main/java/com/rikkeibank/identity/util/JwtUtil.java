package com.rikkeibank.identity.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${rikkeibank.jwt.secret}")
    private String secret;

    @Value("${rikkeibank.jwt.access-token-expiry-ms:900000}")
    private long accessExpiry;

    @Value("${rikkeibank.jwt.refresh-token-expiry-ms:604800000}")
    private long refreshExpiry;

    private javax.crypto.SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String username, String role) {
        return Jwts.builder()
            .subject(username)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessExpiry))
            .signWith(key())
            .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshExpiry))
            .signWith(key())
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser().verifyWith(key()).build()
            .parseSignedClaims(token).getPayload();
    }

    public boolean isValid(String token) {
        try { parseToken(token); return true; }
        catch (Exception e) { return false; }
    }
}
