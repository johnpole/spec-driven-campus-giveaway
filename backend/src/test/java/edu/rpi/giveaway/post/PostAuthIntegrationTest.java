package edu.rpi.giveaway.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.rpi.giveaway.auth.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private CreatePostRequest buildSampleRequest() {
        return new CreatePostRequest(
                "Desk Chair",
                "Comfortable ergonomic chair for dorm study",
                Category.FURNITURE,
                Condition.GOOD,
                new BigDecimal("15.00"),
                "Barton Hall",
                Instant.now().plus(7, ChronoUnit.DAYS),
                "/api/media/test-chair.jpg",
                "Original Name",
                "original@rpi.edu",
                true
        );
    }

    @Test
    @DisplayName("POST /api/posts with valid Bearer token creates post bound to author user ID and email")
    void authenticatedUserCanCreatePost() throws Exception {
        CreatePostRequest request = buildSampleRequest();

        String responseBody = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer test-token:google-user-101:buyer@rpi.edu:Buyer Bob")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.editToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        CreatePostResponse created = objectMapper.readValue(responseBody, CreatePostResponse.class);

        var docOpt = postRepository.findById(created.id());
        assertThat(docOpt).isPresent();
        var doc = docOpt.get();
        assertThat(doc.getAuthorUserId()).isEqualTo("google-user-101");
        assertThat(doc.getAuthorEmail()).isEqualTo("buyer@rpi.edu");
    }

    @Test
    @DisplayName("POST /api/posts without Authorization header returns 401 Unauthorized")
    void unauthenticatedUserCannotCreatePost() throws Exception {
        CreatePostRequest request = buildSampleRequest();

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/posts with invalid Bearer token returns 401 Unauthorized")
    void invalidTokenCannotCreatePost() throws Exception {
        CreatePostRequest request = buildSampleRequest();

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer malformed-token-xyz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
