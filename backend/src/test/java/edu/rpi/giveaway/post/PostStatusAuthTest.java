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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostStatusAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private String createPostAsAuthor(String authorId, String authorEmail) throws Exception {
        CreatePostRequest request = new CreatePostRequest(
                "Microwave",
                "Clean countertop microwave",
                Category.KITCHEN,
                Condition.GOOD,
                new BigDecimal("25.00"),
                "Quad Hall",
                Instant.now().plus(5, ChronoUnit.DAYS),
                "/api/media/microwave.jpg",
                "Author User",
                authorEmail,
                true
        );

        String responseBody = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer test-token:" + authorId + ":" + authorEmail + ":Author User")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(responseBody, CreatePostResponse.class).id();
    }

    @Test
    @DisplayName("Author can transition status to TAKEN, which hides it from public active feed")
    void authorCanTransitionStatusToTaken() throws Exception {
        String authorId = "author-user-501";
        String authorEmail = "author501@rpi.edu";
        String postId = createPostAsAuthor(authorId, authorEmail);

        // Verify active feed initially contains the item
        assertThat(postService.getActiveFeed(null, null, "Microwave")).isNotEmpty();

        // Author transitions status to TAKEN
        mockMvc.perform(patch("/api/posts/" + postId + "/status")
                        .header("Authorization", "Bearer test-token:" + authorId + ":" + authorEmail + ":Author User")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"TAKEN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TAKEN"));

        // Verify item is now excluded from public active feed
        assertThat(postService.getActiveFeed(null, null, "Microwave")
                .stream().noneMatch(p -> p.id().equals(postId))).isTrue();
    }

    @Test
    @DisplayName("Non-author cannot transition post status")
    void nonAuthorCannotTransitionStatus() throws Exception {
        String authorId = "author-user-502";
        String postId = createPostAsAuthor(authorId, "author502@rpi.edu");

        // Another user attempts status update
        mockMvc.perform(patch("/api/posts/" + postId + "/status")
                        .header("Authorization", "Bearer test-token:other-user:other@rpi.edu:Other User")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"TAKEN\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Author can delete post permanently")
    void authorCanDeletePost() throws Exception {
        String authorId = "author-user-503";
        String authorEmail = "author503@rpi.edu";
        String postId = createPostAsAuthor(authorId, authorEmail);

        mockMvc.perform(delete("/api/posts/" + postId)
                        .header("Authorization", "Bearer test-token:" + authorId + ":" + authorEmail + ":Author User"))
                .andExpect(status().isNoContent());

        assertThat(postRepository.findById(postId)).isEmpty();
    }

    @Test
    @DisplayName("Non-author cannot delete post")
    void nonAuthorCannotDeletePost() throws Exception {
        String authorId = "author-user-504";
        String postId = createPostAsAuthor(authorId, "author504@rpi.edu");

        mockMvc.perform(delete("/api/posts/" + postId)
                        .header("Authorization", "Bearer test-token:hacker:hacker@rpi.edu:Hacker"))
                .andExpect(status().isForbidden());

        assertThat(postRepository.findById(postId)).isPresent();
    }

    @Test
    @DisplayName("Unauthenticated deletion without token returns 401")
    void unauthenticatedDeleteReturns401() throws Exception {
        String authorId = "author-user-505";
        String postId = createPostAsAuthor(authorId, "author505@rpi.edu");

        mockMvc.perform(delete("/api/posts/" + postId))
                .andExpect(status().isUnauthorized());
    }
}
