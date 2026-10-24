package edu.rpi.giveaway.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves a bearer token to a {@link UserPrincipal}. Accepts:
 * <ul>
 *   <li>app session tokens issued by {@link AppTokenService} (email/password sign-in),</li>
 *   <li>Google ID tokens, verified against Google's signing keys and our OAuth client ID,</li>
 *   <li>{@code test-token:} strings, only when {@code giveaway.auth.allow-test-tokens=true} (tests).</li>
 * </ul>
 */
@Service
public class GoogleTokenVerifierService {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifierService.class);

    private final AppTokenService appTokenService;
    private final Set<String> adminEmails;
    private final boolean allowTestTokens;
    private final GoogleIdTokenVerifier googleVerifier;

    public GoogleTokenVerifierService(
            AppTokenService appTokenService,
            @Value("${giveaway.auth.google.client-id:}") String googleClientId,
            @Value("${giveaway.auth.admin-emails:}") String adminEmails,
            @Value("${giveaway.auth.allow-test-tokens:false}") boolean allowTestTokens) {
        this.appTokenService = appTokenService;
        this.adminEmails = Arrays.stream(adminEmails.split(","))
                .map(e -> e.trim().toLowerCase())
                .filter(e -> !e.isBlank())
                .collect(Collectors.toUnmodifiableSet());
        this.allowTestTokens = allowTestTokens;
        this.googleVerifier = googleClientId == null || googleClientId.isBlank()
                ? null
                : new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                        .setAudience(List.of(googleClientId.trim()))
                        .build();
    }

    /** Emails granted ADMIN when they sign in with a Google-verified account (ADMIN_EMAILS). */
    public boolean isAdminEmail(String email) {
        return email != null && adminEmails.contains(email.trim().toLowerCase());
    }

    public UserPrincipal verifyToken(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Token is missing or empty");
        }
        if (token.startsWith("test-token:")) {
            return verifyTestToken(token);
        }
        if (appTokenService.isAppToken(token)) {
            return appTokenService.verify(token);
        }
        return verifyGoogleToken(token);
    }

    private UserPrincipal verifyTestToken(String token) {
        if (!allowTestTokens) {
            throw new UnauthorizedException("Test tokens are disabled");
        }
        String[] parts = token.split(":", 4);
        String userId = parts.length > 1 ? parts[1] : "test-user-id";
        String email = parts.length > 2 ? parts[2] : "user@example.com";
        String name = parts.length > 3 ? parts[3] : "Test User";
        return new UserPrincipal(userId, email, name, null, isAdminEmail(email));
    }

    private UserPrincipal verifyGoogleToken(String token) {
        if (googleVerifier == null) {
            throw new UnauthorizedException("Google Sign-In is not configured");
        }
        GoogleIdToken idToken;
        try {
            idToken = googleVerifier.verify(token);
        } catch (Exception e) {
            log.warn("Google ID token verification failed: {}", e.getMessage());
            throw new UnauthorizedException("Invalid Google ID token");
        }
        if (idToken == null) {
            throw new UnauthorizedException("Invalid or expired Google ID token");
        }
        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        if (email == null || !Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new UnauthorizedException("Google account email is not verified");
        }
        String name = payload.get("name") instanceof String n ? n : email.split("@")[0];
        String picture = payload.get("picture") instanceof String p ? p : null;
        return new UserPrincipal(payload.getSubject(), email, name, picture, isAdminEmail(email));
    }
}
