package net.jobdistributor.dashboard.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import net.jobdistributor.dashboard.entity.User;
import net.jobdistributor.dashboard.repository.TokenBlacklistRepository;
import net.jobdistributor.dashboard.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Generate JWT token for authenticated user
     */
    public String generateToken(User user) {
        String primaryEmail = user.getEmails().stream()
                .filter(email -> email.getIsPrimary() && !email.getIsDeleted())
                .map(email -> email.getEmailAddress())
                .findFirst()
                .orElse("unknown@domain.com");

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", primaryEmail)
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("status", user.getStatus().toString())
                .claim("tokenGeneration", user.getTokenGeneration()) // ADD TOKEN GENERATION
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract user ID from JWT token
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            logger.error("Error extracting user ID from token", e);
            return null;
        }
    }

    /**
     * Extract email from JWT token
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get("email", String.class);
        } catch (Exception e) {
            logger.error("Error extracting email from token", e);
            return null;
        }
    }

    /**
     * Validate JWT token - ENHANCED WITH GENERATION CHECK AND BLACKLIST
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 1. Check if token is blacklisted
            String tokenHash = hashToken(token);
            if (tokenBlacklistRepository.existsByTokenHash(tokenHash)) {
                logger.debug("Token is blacklisted");
                return false;
            }

            // 2. Check token generation (for logout-all)
            Long userId = Long.parseLong(claims.getSubject());
            Long tokenGeneration = claims.get("tokenGeneration", Long.class);
            Long currentGeneration = userRepository.getTokenGeneration(userId).orElse(1L);

            if (!tokenGeneration.equals(currentGeneration)) {
                logger.debug("Token generation mismatch - token invalidated by logout-all");
                return false;
            }

            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("JWT token validation error: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true; // Consider expired if we can't parse
        }
    }

    /**
     * Get expiration time from token
     */
    public Date getExpirationFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration();
        } catch (Exception e) {
            logger.error("Error extracting expiration from token", e);
            return null;
        }
    }

    /**
     * Extract bearer token from Authorization header
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Hash token for blacklist storage
     */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            logger.error("Error hashing token", e);
            throw new RuntimeException("Token hashing failed", e);
        }
    }

    /**
     * Generate signing key from secret
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public Date getIssuedAtFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getIssuedAt();
        } catch (Exception e) {
            logger.error("Error extracting issued at from token", e);
            return null;
        }
    }

    public Long getTokenGenerationFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("tokenGeneration", Long.class);
        } catch (Exception e) {
            logger.error("Error extracting token generation from token", e);
            return null;
        }
    }
}