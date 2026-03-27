package co.edu.uniquindio.gestion_solicitudes.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private Key getKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generarToken(String email, String rol, Long userId){
        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getKey())
                .compact();
    }

    public String extraerEmail(String token){
        return parsearClaims(token).getSubject();
    }

    public String extraerRol(String token){
        return parsearClaims(token).get("rol", String.class);
    }

    public Long extraerUserId(String token){
        return parsearClaims(token).get("userId", Long.class);
    }

    public boolean esValido(String token){
        try{
            parsearClaims(token);
            return true;
        } catch (JwtException|IllegalArgumentException e){
            return false;
        }
    }

    private Claims parsearClaims(String token){
        return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
