package edu.rpi.giveaway.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @Test
    void createPostReturns201Created() throws Exception {
        CreatePostRequest request = new CreatePostRequest(
                "Desk Chair",
                "Ergonomic black desk chair",
                Category.FURNITURE,
                Condition.GOOD,
                new BigDecimal("20.00"),
                "Barton Hall",
                Instant.now().plus(5, ChronoUnit.DAYS),
                "/api/media/chair.jpg",
                "Sam Rivera",
                "riveras@rpi.edu",
                true
        );

        when(postService.createPost(any(CreatePostRequest.class)))
                .thenReturn(new CreatePostResponse("item-101", "edit-secret-uuid"));

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer test-token:user-1:riveras@rpi.edu:Sam Rivera")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("item-101"))
                .andExpect(jsonPath("$.editToken").value("edit-secret-uuid"));
    }

    @Test
    void createPostWithMissingFieldsReturns400BadRequest() throws Exception {
        // Missing name and contact
        String invalidJson = """
                {
                    "title": "",
                    "description": "desc",
                    "category": "Furniture",
                    "condition": "GOOD",
                    "price": 0.00,
                    "pickupLocation": "Quad",
                    "availableUntil": "2030-01-01T00:00:00Z",
                    "photoUrl": "/api/media/p.jpg",
                    "legalConfirmed": true
                }
                """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getActiveFeedReturns200WithSummaries() throws Exception {
        PostSummaryResponse item = new PostSummaryResponse(
                "item-1",
                "Desk Chair",
                "Furniture",
                "GOOD",
                new BigDecimal("20.00"),
                false,
                "Barton Hall",
                Instant.now().plus(5, ChronoUnit.DAYS),
                "/api/media/chair.jpg",
                "AVAILABLE",
                1,
                Instant.now()
        );

        when(postService.getActiveFeed(any(), any(), any())).thenReturn(List.of(item));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("item-1"))
                .andExpect(jsonPath("$[0].title").value("Desk Chair"));
    }

    @Test
    void getPostDetailReturns200WithFullDetail() throws Exception {
        PostDetailResponse detail = new PostDetailResponse(
                "item-1",
                "Desk Chair",
                "Ergonomic black desk chair",
                "Furniture",
                "GOOD",
                new BigDecimal("20.00"),
                false,
                "Barton Hall",
                Instant.now().plus(5, ChronoUnit.DAYS),
                "/api/media/chair.jpg",
                "AVAILABLE",
                "Sam",
                1,
                Instant.now()
        );

        when(postService.getPostDetail("item-1")).thenReturn(detail);

        mockMvc.perform(get("/api/posts/item-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("item-1"))
                .andExpect(jsonPath("$.description").value("Ergonomic black desk chair"))
                .andExpect(jsonPath("$.posterName").value("Sam"))
                .andExpect(jsonPath("$.posterContact").doesNotExist());
    }

    @Test
    void updateStatusWithoutTokenReturns401Unauthorized() throws Exception {
        UpdateStatusRequest req = new UpdateStatusRequest(PostStatus.CLAIMED);

        mockMvc.perform(patch("/api/posts/item-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateStatusWithInvalidTokenReturns403Forbidden() throws Exception {
        UpdateStatusRequest req = new UpdateStatusRequest(PostStatus.CLAIMED);

        when(postService.updateStatus(eq("item-1"), eq("wrong-token"), eq(PostStatus.CLAIMED)))
                .thenThrow(new UnauthorizedEditException("Invalid edit token"));

        mockMvc.perform(patch("/api/posts/item-1/status")
                        .header("X-Edit-Token", "wrong-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Invalid edit token"));
    }

    @Test
    void deletePostWithValidTokenReturns204NoContent() throws Exception {
        doNothing().when(postService).deletePost("item-1", "valid-token");

        mockMvc.perform(delete("/api/posts/item-1")
                        .header("X-Edit-Token", "valid-token"))
                .andExpect(status().isNoContent());

        verify(postService).deletePost("item-1", "valid-token");
    }

    @Test
    void deletePostWithoutTokenReturns401Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/posts/item-1"))
                .andExpect(status().isUnauthorized());
    }
}
