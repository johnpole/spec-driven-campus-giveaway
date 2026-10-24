package edu.rpi.giveaway.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Issues and verifies the app's own session tokens (HS256-signed JWTs) for email/password sign-in.
 * Tokens cannot be forged without AUTH_TOKEN_SECRET and expire after {@link #TOKEN_LIFETIME}.
 */
@Service
public class AppTokenService {

    private static final Logger log = LoggerFactory.getLogger(AppTokenService.class);
    private static final String ISSUER = "campus-giveaway";
    private static final Duration TOKEN_LIFETIME = Duration.ofDays(7);
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();

    private final ObjectMapper objectMapper;
    private final byte[] secret;

    public AppTokenService(ObjectMapper objectMapper,
                           @Value("${giveaway.auth.token-secret:}") String configuredSecret) {
        this.objectMapper = objectMapper;
        if (configuredSecret == null || configuredSecret.isBlank()) {
            byte[] random = new byte[32];
            new SecureRandom().nextBytes(random);
            this.secret = random;
            log.warn("AUTH_TOKEN_SECRET is not set; using a random secret. Sessions will reset on restart.");
        } else {
            this.secret = configuredSecret.getBytes(StandardCharsets.UTF_8);
        }
    }

    public String issue(String userId, String email, String name, boolean admin) {
        long now = Instant.now().getEpochSecond();
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", ISSUER);
        claims.put("sub", userId);
        claims.put("email", email);
        claims.put("name", name);
        claims.put("admin", admin);
        claims.put("iat", now);
        claims.put("exp", now + TOKEN_LIFETIME.toSeconds());
        try {
            String header = B64.encodeToString(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
            String payload = B64.encodeToString(objectMapper.writeValueAsBytes(claims));
            return header + "." + payload + "." + sign(header + "." + payload);
        } catch (Exception e) {
            throw new IllegalStateException("Could not issue session token", e);
        }
    }

    /** True when the token was issued by this app (as opposed to a Google ID token). */
    public boolean isAppToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        try {
            JsonNode header = objectMapper.readTree(Base64.getUrlDecoder().decode(parts[0]));
            return "HS256".equals(header.path("alg").asText());
        } catch (Exception e) {
            return false;
        }
    }

    public UserPrincipal verify(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new UnauthorizedException("Invalid session token format");
        }
        byte[] expected = sign(parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new UnauthorizedException("Invalid session token signature");
        }
        try {
            JsonNode claims = objectMapper.readTree(Base64.getUrlDecoder().decode(parts[1]));
            if (!ISSUER.equals(claims.path("iss").asText())) {
                throw new UnauthorizedException("Invalid session token issuer");
            }
            if (Instant.now().getEpochSecond() > claims.path("exp").asLong()) {
                throw new UnauthorizedException("Session token has expired");
            }
            return new UserPrincipal(
                    claims.path("sub").asText(),
                    claims.path("email").asText(),
                    claims.path("name").asText(),
                    null,
                    claims.path("admin").asBoolean(false));
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Malformed session token");
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return B64.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Could not sign session token", e);
        }
    }
}
