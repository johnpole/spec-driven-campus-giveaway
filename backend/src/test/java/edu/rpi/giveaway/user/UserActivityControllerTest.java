package edu.rpi.giveaway.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.rpi.giveaway.auth.UserContext;
import edu.rpi.giveaway.interest.CreateInterestRequest;
import edu.rpi.giveaway.interest.InterestService;
import edu.rpi.giveaway.post.Category;
import edu.rpi.giveaway.post.Condition;
import edu.rpi.giveaway.post.CreatePostRequest;
import edu.rpi.giveaway.post.CreatePostResponse;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InterestService interestService;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private String createPost(String authorId, String authorEmail, String title) throws Exception {
        CreatePostRequest request = new CreatePostRequest(
                title,
                "Description for " + title,
                Category.ELECTRONICS,
                Condition.LIKE_NEW,
                new BigDecimal("50.00"),
                "E-Complex",
                Instant.now().plus(7, ChronoUnit.DAYS),
                "/api/media/elec.jpg",
                "User Name",
                authorEmail,
                true
        );

        String res = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer test-token:" + authorId + ":" + authorEmail + ":User Name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(res, CreatePostResponse.class).id();
    }

    @Test
    @DisplayName("GET /api/users/me/posts returns authored posts and incoming inquiries")
    void getMyPostsReturnsAuthoredPostsWithInquiries() throws Exception {
        String userId = "dashboard-user-1";
        String userEmail = "dash1@rpi.edu";
        String postId = createPost(userId, userEmail, "Gaming Headset");

        // Buyer expresses interest
        mockMvc.perform(post("/api/posts/" + postId + "/interests")
                        .header("Authorization", "Bearer test-token:buyer-2:buyer2@rpi.edu:Buyer Two")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Can pick up today!\"}"))
                .andExpect(status().isCreated());

        // User retrieves their own posts
        mockMvc.perform(get("/api/users/me/posts")
                        .header("Authorization", "Bearer test-token:" + userId + ":" + userEmail + ":Dashboard User"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Gaming Headset"))
                .andExpect(jsonPath("$[0].inquiries[0].visitorName").value("Buyer Two"))
                .andExpect(jsonPath("$[0].inquiries[0].message").value("Can pick up today!"));
    }

    @Test
    @DisplayName("GET /api/users/me/interests returns items requested by user")
    void getMyInterestsReturnsRequestedItems() throws Exception {
        String sellerId = "seller-9";
        String postId = createPost(sellerId, "seller9@rpi.edu", "Mini Fridge");

        String buyerId = "buyer-active-9";
        String buyerEmail = "buyer9@rpi.edu";

        // Buyer expresses interest
        mockMvc.perform(post("/api/posts/" + postId + "/interests")
                        .header("Authorization", "Bearer test-token:" + buyerId + ":" + buyerEmail + ":Buyer Nine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Is the freezer cold?\"}"))
                .andExpect(status().isCreated());

        // Buyer checks their requested items
        mockMvc.perform(get("/api/users/me/interests")
                        .header("Authorization", "Bearer test-token:" + buyerId + ":" + buyerEmail + ":Buyer Nine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemTitle").value("Mini Fridge"))
                .andExpect(jsonPath("$[0].message").value("Is the freezer cold?"));
    }

    @Test
    @DisplayName("Unauthenticated requests to /api/users/me/* return 401")
    void unauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/users/me/posts"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users/me/interests"))
                .andExpect(status().isUnauthorized());
    }
}
