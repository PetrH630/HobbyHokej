package cz.phsoft.hokej.security;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String JWT_SECRET = "tajnyKlic123";
    private final long JWT_EXPIRATION = 86400000L; // 1 den

    public String generateToken(String email, String role, String type) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("type", type)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(token).getBody().getSubject();
    }

    public String getRoleFromToken(String token) {
        return (String) Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(token).getBody().get("role");
    }

    public String getTypeFromToken(String token) {
        return (String) Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(token).getBody().get("type");
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
