package com.onlinefoodorder.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * Utility class for generating, extracting, and validating JWT tokens.
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Generates a secure signing key for JWT.
     *
     * @return The generated signing key.
     */
    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Generates a JWT token for the given email.
     *
     * @param email The email of the authenticated user.
     * @return The generated JWT token.
     */
    public String generateToken(String email) {
        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1-hour validity
                .signWith(getSignKey(), SignatureAlgorithm.HS512) // Using HS512
                .compact();
        logger.info("Generated JWT token for user: {}", email);
        return token;
    }

    /**
     * Extracts the email (subject) from the given JWT token.
     *
     * @param token The JWT token.
     * @return The email extracted from the token.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the given JWT token.
     *
     * @param token The JWT token.
     * @return The expiration date of the token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the JWT token.
     *
     * @param token         The JWT token.
     * @param claimsResolver Function to resolve claims.
     * @param <T>           The type of the claim to be extracted.
     * @return The extracted claim.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = parseToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the JWT token and retrieves its claims.
     *
     * @param token The JWT token.
     * @return The claims contained in the token.
     */
    private Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token expired: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException | SignatureException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error parsing JWT token: {}", e.getMessage());
            throw new RuntimeException("Error processing JWT token");
        }
    }

    /**
     * Validates a JWT token against a provided user email.
     *
     * @param token    The JWT token.
     * @param userEmail The email of the user.
     * @return true if the token is valid, false otherwise.
     */
    public boolean validateToken(String token, String userEmail) {
        try {
            final String extractedEmail = extractEmail(token);
            boolean isValid = (extractedEmail.equals(userEmail) && !isTokenExpired(token));
            if (isValid) {
                logger.info("JWT token successfully validated for user: {}", userEmail);
            } else {
                logger.warn("JWT validation failed for user: {}", userEmail);
            }
            return isValid;
        } catch (Exception e) {
            logger.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a JWT token has expired.
     *
     * @param token The JWT token.
     * @return true if the token is expired, false otherwise.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
