package reset.reset.Security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.issuer:erp-system}")
    private String issuer;

    /**
     * Extract username from JWT token
     */
    public String extractUsername(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extract all claims from token
     */
    public DecodedJWT decodeToken(String token) {
        try {
            return JWT.decode(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Generate token with default claims
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generate token with extra claims
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        Algorithm algorithm = Algorithm.HMAC256(secret);

        // Build claims
        var builder = JWT.create()
                .withIssuer(issuer)
                .withSubject(userDetails.getUsername())
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(Instant.now().plus(expiration, ChronoUnit.MILLIS)))
                .withClaim("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));

        // Add extra claims
        extraClaims.forEach((key, value) -> {
            if (value instanceof String) {
                builder.withClaim(key, (String) value);
            } else if (value instanceof Integer) {
                builder.withClaim(key, (Integer) value);
            } else if (value instanceof Long) {
                builder.withClaim(key, (Long) value);
            } else if (value instanceof Boolean) {
                builder.withClaim(key, (Boolean) value);
            } else if (value instanceof Map) {
                builder.withClaim(key, (Map) value);
            } else if (value instanceof String[]) {
                builder.withArrayClaim(key, (String[]) value);
            } else if (value instanceof Integer[]) {
                builder.withArrayClaim(key, (Integer[]) value);
            } else if (value instanceof Long[]) {
                builder.withArrayClaim(key, (Long[]) value);
            }
        });

        return builder.sign(algorithm);
    }

    /**
     * Generate token with user ID and company ID
     */
    public String generateTokenWithUserInfo(UserDetails userDetails, Long userId, Long empresaId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("empresaId", empresaId);
        claims.put("username", userDetails.getUsername());
        return generateToken(claims, userDetails);
    }

    /**
     * Validate token
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username != null
                    && username.equals(userDetails.getUsername())
                    && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            Date expiresAt = decodedJWT.getExpiresAt();
            return expiresAt != null && expiresAt.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Get expiration date from token
     */
    public Date extractExpiration(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getExpiresAt();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get a specific claim from token
     */
    public <T> T extractClaim(String token, String claimName, Class<T> type) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getClaim(claimName).as(type);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get all claims as map
     */
    public Map<String, Object> extractAllClaims(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            Map<String, Object> claims = new HashMap<>();
            decodedJWT.getClaims().forEach((key, value) -> {
                claims.put(key, value.as(Object.class));
            });
            return claims;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * Verify token signature and validity
     */
    public boolean verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * Get decoded JWT with verification
     */
    public DecodedJWT verifyAndDecode(String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
        return verifier.verify(token);
    }

    /**
     * Refresh token (extend expiration)
     */
    public String refreshToken(String token) {
        if (!verifyToken(token)) {
            throw new IllegalArgumentException("Invalid token");
        }

        DecodedJWT decodedJWT = JWT.decode(token);
        Algorithm algorithm = Algorithm.HMAC256(secret);

        var builder = JWT.create()
                .withIssuer(issuer)
                .withSubject(decodedJWT.getSubject())
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(Instant.now().plus(expiration, ChronoUnit.MILLIS)));

        // Copy all claims from original token
        decodedJWT.getClaims().forEach((key, value) -> {
            if (!key.equals("iss") && !key.equals("sub") &&
                    !key.equals("iat") && !key.equals("exp")) {
                builder.withClaim(key, (Boolean) value.as(Object.class));
            }
        });

        return builder.sign(algorithm);
    }

    /**
     * Get remaining time in milliseconds
     */
    public long getRemainingTime(String token) {
        Date expiration = extractExpiration(token);
        if (expiration == null) return 0;
        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * Check if token can be refreshed (less than 50% of expiration time remaining)
     */
    public boolean shouldRefresh(String token) {
        long remaining = getRemainingTime(token);
        return remaining < (expiration / 2);
    }
}