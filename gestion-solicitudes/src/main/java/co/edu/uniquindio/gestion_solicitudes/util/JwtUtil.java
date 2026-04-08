package co.edu.uniquindio.gestion_solicitudes.util;

import co.edu.uniquindio.gestion_solicitudes.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private Key getKey(){
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String generarToken(String email, String rol, Long userId){
        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
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
        return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
