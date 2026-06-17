package com.scrapDetection.security.jwt;

import com.scrapDetection.entity.Account;
import com.scrapDetection.exception.InvalidRequestException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret:your-super-secret-key-at-least-64-characters-for-hs512-security-change-in-production}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24 hours
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Account account) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", account.getRole().name());
        claims.put("accountId", account.getAccountId());

        return Jwts.builder().claims(claims).subject(account.getPhoneNumbers()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractPhoneNumber(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractAccountId(String token) {
        return extractAllClaims(token).get("accountId", Long.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token, Account account) {
        final String phoneNumber = extractPhoneNumber(token);
        return phoneNumber.equals(account.getPhoneNumbers()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())      // ← Updated for 0.12.6+
                    .build()
                    .parseSignedClaims(token)         // ← Updated for 0.12.6+
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new InvalidRequestException("JWT token has expired");
        } catch (JwtException e) {
            throw new InvalidRequestException("Invalid JWT token");
        }
    }

    public String getTokenHash(String token) {
        if (token == null) return null;
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }
}