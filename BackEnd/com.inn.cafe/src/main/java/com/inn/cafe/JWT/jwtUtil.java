package com.inn.cafe.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class jwtUtil {
    // Configured via cafe.jwt.secret (env var JWT_SECRET in production). Must be >= 256 bits
    // for HS256 (jjwt 0.11.x enforces this) - see application.properties for the dev default.
    @Value("${cafe.jwt.secret}")
    private String secret;

    private Key signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token){
        return extractClamis(token , Claims::getSubject);
    }

    public Date extractExpiration(String token){
        return extractClamis(token , Claims::getExpiration);
    }

    private <T> T extractClamis(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);

    }
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(signingKey()).build().parseClaimsJws(token).getBody();
    }
    private Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String Username , String role){
        Map<String , Object> claims = new HashMap<>();
        claims.put("role" , role);
        return createtoken(claims, Username);
    }
    private String createtoken(Map<String , Object> claims , String subject){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000*60*60*10))
                .signWith(signingKey()).compact();
    }
    public Boolean validatetoken(String token , UserDetails userDetails){
        final String Username = extractUsername(token);
        return (Username.equals(userDetails.getUsername()) && !isTokenExpired(token) );
    }

}
