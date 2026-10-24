package edu.rpi.giveaway.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.rpi.giveaway.auth.UserContext;
import edu.rpi.giveaway.auth.UserPrincipal;
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
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostAdminModerationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostService postService;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("Admin can delete any user's post without being the author")
    void adminCanDeleteAnyPost() throws Exception {
        // 1. Author creates post
        UserContext.setCurrentUser(new UserPrincipal("author-123", "student@rpi.edu", "Student Author", null, false));
        CreatePostRequest createReq = new CreatePostRequest(
                "Desk Chair",
                "Ergonomic chair in Barton Hall",
                Category.FURNITURE,
                Condition.LIKE_NEW,
                BigDecimal.ZERO,
                "Barton Hall",
                Instant.now().plus(7, ChronoUnit.DAYS),
                "/api/media/chair.jpg",
                "Student Author",
                "student@rpi.edu",
                true
        );
        CreatePostResponse post = postService.createPost(createReq);
        UserContext.clear();

        // 2. Non-author user attempt fails with 403 Forbidden
        mockMvc.perform(delete("/api/posts/" + post.id())
                        .header("Authorization", "Bearer test-token:other-user:other@rpi.edu:Other User"))
                .andExpect(status().isForbidden());

        // 3. Admin user (admin@campus-giveaway.test) deletes successfully with 204 No Content
        mockMvc.perform(delete("/api/posts/" + post.id())
                        .header("Authorization", "Bearer test-token:admin-user:admin@campus-giveaway.test:John Pole"))
                .andExpect(status().isNoContent());

        // 4. Verify post is deleted
        mockMvc.perform(get("/api/posts/" + post.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Admin can update status of any user's post")
    void adminCanUpdateStatusOfAnyPost() throws Exception {
        // 1. Author creates post
        UserContext.setCurrentUser(new UserPrincipal("author-456", "student2@rpi.edu", "Student Two", null, false));
        CreatePostRequest createReq = new CreatePostRequest(
                "Mini Fridge",
                "Working compact fridge",
                Category.KITCHEN,
                Condition.GOOD,
                new BigDecimal("15.00"),
                "Quad Hall",
                Instant.now().plus(7, ChronoUnit.DAYS),
                "/api/media/fridge.jpg",
                "Student Two",
                "student2@rpi.edu",
                true
        );
        CreatePostResponse post = postService.createPost(createReq);
        UserContext.clear();

        // 2. Admin marks it as TAKEN
        mockMvc.perform(patch("/api/posts/" + post.id() + "/status")
                        .header("Authorization", "Bearer test-token:admin-user:admin@campus-giveaway.test:John Pole")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "TAKEN"))))
                .andExpect(status().isOk());
    }
}
