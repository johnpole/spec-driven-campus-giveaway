package edu.rpi.giveaway.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenVerificationTest {

    private static final String CLIENT_ID = "test-client.apps.googleusercontent.com";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AppTokenService appTokens;
    private GoogleTokenVerifierService tokenVerifier;
    private GoogleAuthFilter authFilter;

    @BeforeEach
    void setUp() {
        appTokens = new AppTokenService(objectMapper, "unit-test-secret");
        tokenVerifier = new GoogleTokenVerifierService(appTokens, CLIENT_ID, "admin@rpi.edu", true);
        authFilter = new GoogleAuthFilter(tokenVerifier, objectMapper);
        UserContext.clear();
    }

    private String unsignedJwt(String alg, Map<String, Object> claims) throws Exception {
        Base64.Encoder b64 = Base64.getUrlEncoder().withoutPadding();
        String header = b64.encodeToString(("{\"alg\":\"" + alg + "\",\"typ\":\"JWT\"}").getBytes(StandardCharsets.UTF_8));
        String payload = b64.encodeToString(objectMapper.writeValueAsBytes(claims));
        return header + "." + payload + ".forged-signature";
    }

    @Test
    @DisplayName("Valid test-token string resolves UserPrincipal when test tokens are enabled")
    void validTestTokenResolvesUserPrincipal() {
        UserPrincipal user = tokenVerifier.verifyToken("test-token:google-123:test@rpi.edu:John Doe");
        assertThat(user.userId()).isEqualTo("google-123");
        assertThat(user.email()).isEqualTo("test@rpi.edu");
        assertThat(user.name()).isEqualTo("John Doe");
        assertThat(user.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("Test tokens are rejected when disabled (production default)")
    void testTokensRejectedWhenDisabled() {
        GoogleTokenVerifierService prod = new GoogleTokenVerifierService(appTokens, CLIENT_ID, "admin@rpi.edu", false);
        assertThatThrownBy(() -> prod.verifyToken("test-token:1:admin@rpi.edu:Mallory"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("App session token issued at sign-in round-trips with its admin flag")
    void appTokenRoundTrips() {
        String token = appTokens.issue("u-1", "admin@rpi.edu", "Ada Admin", true);
        UserPrincipal user = tokenVerifier.verifyToken(token);
        assertThat(user.userId()).isEqualTo("u-1");
        assertThat(user.email()).isEqualTo("admin@rpi.edu");
        assertThat(user.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("Tampered app token (admin flag flipped) is rejected")
    void tamperedAppTokenRejected() throws Exception {
        String token = appTokens.issue("u-2", "student@rpi.edu", "Sam", false);
        String[] parts = token.split("\\.");
        Map<String, Object> forged = Map.of("iss", "campus-giveaway", "sub", "u-2", "email", "student@rpi.edu",
                "name", "Sam", "admin", true, "exp", Instant.now().plusSeconds(3600).getEpochSecond());
        String forgedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(forged));
        assertThatThrownBy(() -> tokenVerifier.verifyToken(parts[0] + "." + forgedPayload + "." + parts[2]))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("signature");
    }

    @Test
    @DisplayName("App token signed with a different secret is rejected")
    void appTokenFromOtherSecretRejected() {
        String token = new AppTokenService(objectMapper, "attacker-secret").issue("u-3", "admin@rpi.edu", "Eve", true);
        assertThatThrownBy(() -> tokenVerifier.verifyToken(token)).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Legacy unsigned cg-token strings are rejected")
    void legacyUnsignedTokenRejected() {
        assertThatThrownBy(() -> tokenVerifier.verifyToken("cg-token:x:admin@rpi.edu:Mallory:true:0"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Forged Google ID token without a valid Google signature is rejected")
    void forgedGoogleTokenRejected() throws Exception {
        String forged = unsignedJwt("RS256", Map.of(
                "iss", "https://accounts.google.com", "aud", CLIENT_ID, "sub", "123",
                "email", "admin@rpi.edu", "email_verified", true,
                "exp", Instant.now().plusSeconds(3600).getEpochSecond()));
        assertThatThrownBy(() -> tokenVerifier.verifyToken(forged)).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Malformed token string throws UnauthorizedException")
    void malformedTokenThrowsUnauthorized() {
        assertThatThrownBy(() -> tokenVerifier.verifyToken("not-a-valid-token"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("GoogleAuthFilter populates UserContext on valid token")
    void filterPopulatesUserContextOnValidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + appTokens.issue("user-999", "test@example.com", "Alex", false));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        authFilter.doFilter(request, response, (req, res) -> {
            UserPrincipal current = UserContext.getCurrentUser();
            assertThat(current).isNotNull();
            assertThat(current.userId()).isEqualTo("user-999");
            assertThat(current.email()).isEqualTo("test@example.com");
            filterChain.doFilter(req, res);
        });

        assertThat(UserContext.getCurrentUser()).isNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("GoogleAuthFilter returns 401 on invalid Bearer token")
    void filterReturns401OnInvalidBearerToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer malformed.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        authFilter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Unauthorized");
    }
}
