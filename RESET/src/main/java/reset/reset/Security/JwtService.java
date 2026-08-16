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
import reset.reset.Models.auth.User;

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

    public String extractUsername(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public DecodedJWT decodeToken(String token) {
        try {
            return JWT.decode(token);
        } catch (Exception e) {
            return null;
        }
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

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

        // Adicionar claims extras se for UserPrincipal
        if (userDetails instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) userDetails;
            builder.withClaim("userId", userPrincipal.getId());
            if (userPrincipal.getEmpresaId() != null) {
                builder.withClaim("empresaId", userPrincipal.getEmpresaId());
            }
            if (userPrincipal.getNome() != null) {
                builder.withClaim("nome", userPrincipal.getNome());
            }
            if (userPrincipal.getEmail() != null) {
                builder.withClaim("email", userPrincipal.getEmail());
            }
        }

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

    public String generateTokenFromUser(User user) {
        // Inicializar coleções para evitar problemas de Lazy Loading
        if (user.getRoles() != null) {
            user.getRoles().size();
            user.getRoles().forEach(role -> {
                if (role.getPermissoes() != null) {
                    role.getPermissoes().size();
                }
            });
        }
        if (user.getPermissoes() != null) {
            user.getPermissoes().size();
        }

        Algorithm algorithm = Algorithm.HMAC256(secret);

        var builder = JWT.create()
                .withIssuer(issuer)
                .withSubject(user.getUsername())
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(Instant.now().plus(expiration, ChronoUnit.MILLIS)))
                .withClaim("roles", user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .withClaim("userId", user.getId())
                .withClaim("empresaId", user.getEmpresa().getId())
                .withClaim("nome", user.getNome() != null ? user.getNome() : "")
                .withClaim("email", user.getEmail() != null ? user.getEmail() : "");

        return builder.sign(algorithm);
    }

    public String generateTokenWithUserInfo(UserDetails userDetails, Long userId, Long empresaId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("empresaId", empresaId);
        claims.put("username", userDetails.getUsername());
        return generateToken(claims, userDetails);
    }

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

    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            Date expiresAt = decodedJWT.getExpiresAt();
            return expiresAt != null && expiresAt.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Date extractExpiration(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getExpiresAt();
        } catch (Exception e) {
            return null;
        }
    }

    public <T> T extractClaim(String token, String claimName, Class<T> type) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getClaim(claimName).as(type);
        } catch (Exception e) {
            return null;
        }
    }

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

    public DecodedJWT verifyAndDecode(String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
        return verifier.verify(token);
    }

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

    public long getRemainingTime(String token) {
        Date expiration = extractExpiration(token);
        if (expiration == null) return 0;
        return expiration.getTime() - System.currentTimeMillis();
    }

    public boolean shouldRefresh(String token) {
        long remaining = getRemainingTime(token);
        return remaining < (expiration / 2);
    }
}