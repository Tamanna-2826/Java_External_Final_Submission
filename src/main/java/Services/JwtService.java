/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

/**
 *
 * @author LENOVO
 */
@ApplicationScoped
public class JwtService {

    private static final String SECRET_KEY = "mySecretKeyForJWTTokenGenerationThatIsLongEnoughForHS256Algorithm";
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 hours
    private static final long REFRESH_TIME = 2 * 60 * 60 * 1000; // 2 hours

    private final SecretKey key;

    public JwtService() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * Generate JWT token with claims
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        try {
            Date now = new Date();
            Date expiration = new Date(now.getTime() + EXPIRATION_TIME);

            JwtBuilder builder = Jwts.builder()
                    .setSubject(subject)
                    .setIssuedAt(now)
                    .setExpiration(expiration)
                    .signWith(key, SignatureAlgorithm.HS256);

            // Add custom claims
            if (claims != null && !claims.isEmpty()) {
                for (Map.Entry<String, Object> entry : claims.entrySet()) {
                    builder.claim(entry.getKey(), entry.getValue());
                }
            }

            return builder.compact();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate simple token with just subject
     */
    public String generateToken(String subject) {
        return generateToken(subject, null);
    }

    /**
     * Validate JWT token
     */
    public boolean isTokenValid(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }

            // Remove "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (ExpiredJwtException e) {
            System.out.println("JWT token is expired: " + e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT token is unsupported: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.out.println("JWT token is malformed: " + e.getMessage());
            return false;
        } catch (SecurityException e) {
            System.out.println("JWT signature validation failed: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.out.println("JWT token compact of handler are invalid: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("JWT token validation error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extract subject from token
     */
    public String getSubjectFromToken(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extract all claims from token
     */
    public Claims getClaimsFromToken(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extract specific claim from token
     */
    public Object getClaimFromToken(String token, String claimKey) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null ? claims.get(claimKey) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims != null) {
                Date expiration = claims.getExpiration();
                return expiration.before(new Date());
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Check if token needs refresh (within 2 hours of expiration)
     */
    public boolean needsRefresh(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims != null) {
                Date expiration = claims.getExpiration();
                Date now = new Date();
                long timeUntilExpiration = expiration.getTime() - now.getTime();
                return timeUntilExpiration <= REFRESH_TIME;
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Get user ID from token
     */
    public Long getUserIdFromToken(String token) {
        try {
            Object userId = getClaimFromToken(token, "userId");
            return userId != null ? Long.valueOf(userId.toString()) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get user role from token
     */
    public String getRoleFromToken(String token) {
        try {
            Object role = getClaimFromToken(token, "role");
            return role != null ? role.toString() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get user email from token
     */
    public String getEmailFromToken(String token) {
        try {
            return getSubjectFromToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
