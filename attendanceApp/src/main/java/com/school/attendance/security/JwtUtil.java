package com.school.attendance.security;

import com.school.attendance.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtUtil {

    private final AppProperties appProperties;

    public JwtUtil(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    void validateJwtConfig() {
        String secret = appProperties.getJwt().getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret must be configured");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes for HS256 signing");
        }
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private long getExpirationMillis() {
        int expiryMinutes = appProperties.getJwt().getExpiryMinutes() == null
                ? 60
                : appProperties.getJwt().getExpiryMinutes();
        return Duration.ofMinutes(expiryMinutes).toMillis();
    }

    public String generateToken(String username, String role) {
        return generateToken(username, role, "DEMO", null);
    }

    public String generateToken(String username, String role, String schoolCode, Long userId) {
        return Jwts.builder()
                .subject(username)
                .claim("role", SecurityAccess.normalizeRole(role))
                .claim("school_id", schoolCode == null ? "DEMO" : schoolCode.toUpperCase())
                .claim("user_id", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + getExpirationMillis()))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) { return extractClaims(token).getSubject(); }
    public String extractRole(String token) { return extractClaims(token).get("role", String.class); }
    public String extractSchoolId(String token) {
        String schoolId = extractClaims(token).get("school_id", String.class);
        return schoolId == null ? "DEMO" : schoolId;
    }

    public boolean isTokenValid(String token) {
        return extractClaims(token).getExpiration().after(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
