package org.example.tasktrackerserver.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String SECRET_KEY = "dnkjsdbhjbbdfdkjsbaefkljsdbfhjurgeabnqsdflbrleibfjelis";  // Замените на свой ключ
    private static final long EXPIRATION_TIME = 86400000;

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // Генерация токена
    public String generateToken(String username, String role, Long userId ) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", role)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }


    // Проверка токена на валидность
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // Извлечение имени пользователя из токена
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        return (String) Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody().get("roles");
    }

    public Long getIDFromToken(String token) {
        Object userId = Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody().get("userId");
        return userId instanceof Integer ? ((Integer) userId).longValue() : (Long) userId;
    }


}
