package com.ecommerce.user.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "YourSecretKeyYourSecretKeyYourSecretKeyYourSecretKey";
    private static final long EXPIRATION_TIME = 86400000; // 1 giorno

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, List<String> roles) {
        List<String> prefixedRoles = roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .collect(Collectors.toList());
        return Jwts.builder()
                .setSubject(username)  // Nuovo metodo per impostare il subject
                .claim("roles", prefixedRoles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Nuova API per la firma
                .compact();
    }

    public String validateToken(String token) {
        Claims claims = Jwts.parserBuilder() // Nuova API per il parsing
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public List<SimpleGrantedAuthority> getRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        Object rolesObject = claims.get("roles");
        List<String> roles;
        if (rolesObject instanceof String) {
            roles = Arrays.asList(((String) rolesObject).split(","));
        } else {
            roles = new ObjectMapper().convertValue(rolesObject, new TypeReference<List<String>>() {});
        }
        return roles.stream()
                .map(SimpleGrantedAuthority::new) // Ora ritorna direttamente SimpleGrantedAuthority
                .collect(Collectors.toList());
    }
}