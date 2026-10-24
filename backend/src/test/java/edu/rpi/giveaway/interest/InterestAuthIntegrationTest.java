package edu.rpi.giveaway.interest;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.rpi.giveaway.auth.UserContext;
import edu.rpi.giveaway.post.*;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InterestAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostService postService;

    @Autowired
    private InterestRepository interestRepository;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private String createPost(String authorId, String authorEmail) throws Exception {
        CreatePostRequest request = new CreatePostRequest(
                "Textbook Calc 2",
                "Calculus Early Transcendentals",
                Category.TEXTBOOKS,
                Condition.GOOD,
                new BigDecimal("10.00"),
                "DCC 308",
                Instant.now().plus(7, ChronoUnit.DAYS),
                "/api/media/calc.jpg",
                "Author Student",
                authorEmail,
                true
        );

        String res = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer test-token:" + authorId + ":" + authorEmail + ":Author Student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(res, CreatePostResponse.class).id();
    }

    @Test
    @DisplayName("Authenticated buyer submits single-field message note, auto-populating buyer contact")
    void authenticatedBuyerCanSubmitInterestWithMessageOnly() throws Exception {
        String postId = createPost("poster-1", "poster1@rpi.edu");

        String inquiryJson = "{\"message\":\"Can pick up tomorrow at 2pm!\"}";

        mockMvc.perform(post("/api/posts/" + postId + "/interests")
                        .header("Authorization", "Bearer test-token:buyer-1:buyer1@rpi.edu:Buyer Bob")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inquiryJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalCount").value(1));

        List<InterestDocument> inquiries = interestRepository.findByPostId(postId);
        assertThat(inquiries).hasSize(1);
        InterestDocument saved = inquiries.get(0);
        assertThat(saved.getInquirerUserId()).isEqualTo("buyer-1");
        assertThat(saved.getInquirerEmail()).isEqualTo("buyer1@rpi.edu");
        assertThat(saved.getVisitorName()).isEqualTo("Buyer Bob");
        assertThat(saved.getMessage()).isEqualTo("Can pick up tomorrow at 2pm!");
    }

    @Test
    @DisplayName("Author cannot submit interest on their own post")
    void authorCannotSubmitInterestOnOwnPost() throws Exception {
        String authorId = "poster-self";
        String postId = createPost(authorId, "poster@rpi.edu");

        String inquiryJson = "{\"message\":\"I want my own item\"}";

        mockMvc.perform(post("/api/posts/" + postId + "/interests")
                        .header("Authorization", "Bearer test-token:" + authorId + ":poster@rpi.edu:Author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inquiryJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Unauthenticated visitor without contact info is rejected with 401")
    void unauthenticatedWithoutContactRejected() throws Exception {
        String postId = createPost("poster-xyz", "xyz@rpi.edu");

        String inquiryJson = "{\"message\":\"Interested!\"}";

        mockMvc.perform(post("/api/posts/" + postId + "/interests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inquiryJson))
                .andExpect(status().isUnauthorized());
    }
}
