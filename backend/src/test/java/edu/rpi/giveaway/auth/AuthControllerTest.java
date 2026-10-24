package edu.rpi.giveaway.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private edu.rpi.giveaway.user.UserAccountRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Should successfully register a new student user")
    void shouldRegisterNewUser() throws Exception {
        String uniqueEmail = "student_" + System.currentTimeMillis() + "@rpi.edu";
        RegisterRequest req = new RegisterRequest(
                "Mike",
                "Rounds",
                uniqueEmail,
                "518-555-0199",
                "pass123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.user.email", is(uniqueEmail)))
                .andExpect(jsonPath("$.user.firstName", is("Mike")))
                .andExpect(jsonPath("$.user.isAdmin", is(false)));
    }

    @Test
    @DisplayName("Self-registration never grants ADMIN, even with an admin-listed email")
    void registrationNeverGrantsAdmin() throws Exception {
        String email = "admin-claim-" + System.currentTimeMillis() + "@rpi.edu";
        RegisterRequest req = new RegisterRequest("Mallory", "Claims", email, null, "pass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.isAdmin", is(false)));
    }

    @Test
    @DisplayName("Admin account seeded from configuration signs in with ADMIN")
    void seededAdminSignsInAsAdmin() throws Exception {
        String adminEmail = "seeded-admin@rpi.edu";
        new AdminAccountSeeder(userRepository, passwordEncoder, adminEmail, "seeded-pass", "Seed Admin").run();

        LoginRequest loginReq = new LoginRequest(adminEmail, "seeded-pass");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.isAdmin", is(true)))
                .andExpect(jsonPath("$.user.email", is(adminEmail)));
    }

    @Test
    @DisplayName("Should reject login with incorrect password")
    void shouldRejectInvalidPassword() throws Exception {
        String email = "wrongpass_" + System.currentTimeMillis() + "@rpi.edu";
        RegisterRequest req = new RegisterRequest("Alice", "Smith", email, null, "secret99");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        LoginRequest badLogin = new LoginRequest(email, "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized());
    }
}
