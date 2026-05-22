package com.school.attendance.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET =
            "this-is-a-very-secret-key-for-school-attendance-app-123456";

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 8; // 8 hours for pilot testing

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
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
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
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
