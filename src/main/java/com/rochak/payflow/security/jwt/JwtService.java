package com.rochak.payflow.security.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis()+jwtExpiration)
                )
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token){
        return extractClaims(token).getSubject();
    }

    public String extractSessionId(String token){
        return extractClaims(token).get("sid", String.class);
    }

    public String extractToken(String token){
        return extractClaims(token).getId();
    }

    public boolean isTokenValid(String token){
        try{
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String generateRefreshToken(String email, String sessionId, String tokenId) {
        return Jwts.builder()
                .subject(email)
                .id(tokenId)
                .claim("sid", sessionId)
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + refreshExpiration)
                )
                .signWith(key)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
