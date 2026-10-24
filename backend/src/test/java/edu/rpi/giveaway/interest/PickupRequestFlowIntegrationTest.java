package edu.rpi.giveaway.interest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.rpi.giveaway.post.Category;
import edu.rpi.giveaway.post.Condition;
import edu.rpi.giveaway.post.CreatePostRequest;
import edu.rpi.giveaway.post.CreatePostResponse;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The pickup conversation: a request with time and place, one per person, and replies both ways. */
@SpringBootTest
@AutoConfigureMockMvc
class PickupRequestFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String run = UUID.randomUUID().toString().substring(0, 8);
    private final String seller = "Bearer test-token:seller-" + run + ":seller" + run + "@rpi.edu:Sally Seller";
    private final String buyer = "Bearer test-token:buyer-" + run + ":buyer" + run + "@rpi.edu:Ben Buyer";
    private final String stranger = "Bearer test-token:stranger-" + run + ":stranger" + run + "@rpi.edu:Stan Stranger";

    private String createPost() throws Exception {
        CreatePostRequest request = new CreatePostRequest("Mini fridge " + run, "Works great", Category.KITCHEN,
                Condition.GOOD, BigDecimal.ZERO, "Commons", Instant.now().plus(10, ChronoUnit.DAYS),
                "/api/media/" + UUID.randomUUID() + ".jpg", "x", "x@rpi.edu", true);
        String body = mockMvc.perform(post("/api/posts").header("Authorization", seller)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(body, CreatePostResponse.class).id();
    }

    private String requestBody(Instant pickupAt, String place) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "message", "Can I grab it Friday?", "pickupAt", pickupAt.toString(), "pickupPlace", place));
    }

    private JsonNode myPosts(String auth) throws Exception {
        return objectMapper.readTree(mockMvc.perform(get("/api/users/me/posts").header("Authorization", auth))
                .andReturn().getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Seller sees the requester's name, contact, pickup time and place")
    void sellerSeesMeaningfulRequest() throws Exception {
        String postId = createPost();
        Instant pickupAt = Instant.now().plus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

        mockMvc.perform(post("/api/posts/" + postId + "/interests").header("Authorization", buyer)
                        .contentType(MediaType.APPLICATION_JSON).content(requestBody(pickupAt, "Union lobby")))
                .andExpect(status().isCreated());

        JsonNode inquiry = null;
        for (JsonNode p : myPosts(seller)) {
            if (p.get("id").asText().equals(postId)) inquiry = p.get("inquiries").get(0);
        }
        assertThat(inquiry).isNotNull();
        assertThat(inquiry.get("visitorName").asText()).isEqualTo("Ben Buyer");
        assertThat(inquiry.get("visitorContact").asText()).isEqualTo("buyer" + run + "@rpi.edu");
        assertThat(inquiry.get("pickupPlace").asText()).isEqualTo("Union lobby");
        assertThat(Instant.parse(inquiry.get("pickupAt").asText())).isEqualTo(pickupAt);
    }

    @Test
    @DisplayName("A second request from the same person is refused (409)")
    void duplicateRequestRefused() throws Exception {
        String postId = createPost();
        Instant pickupAt = Instant.now().plus(1, ChronoUnit.DAYS);
        mockMvc.perform(post("/api/posts/" + postId + "/interests").header("Authorization", buyer)
                .contentType(MediaType.APPLICATION_JSON).content(requestBody(pickupAt, "Union")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/posts/" + postId + "/interests").header("Authorization", buyer)
                        .contentType(MediaType.APPLICATION_JSON).content(requestBody(pickupAt, "Union")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("You already sent a request for this item"));
    }

    @Test
    @DisplayName("Item detail tells the viewer if they own it or already requested it")
    void detailViewerFlags() throws Exception {
        String postId = createPost();
        mockMvc.perform(get("/api/posts/" + postId).header("Authorization", seller))
                .andExpect(jsonPath("$.ownedByViewer").value(true))
                .andExpect(jsonPath("$.requestedByViewer").value(false));
        mockMvc.perform(get("/api/posts/" + postId).header("Authorization", buyer))
                .andExpect(jsonPath("$.ownedByViewer").value(false))
                .andExpect(jsonPath("$.requestedByViewer").value(false));

        mockMvc.perform(post("/api/posts/" + postId + "/interests").header("Authorization", buyer)
                .contentType(MediaType.APPLICATION_JSON).content(requestBody(Instant.now().plus(1, ChronoUnit.DAYS), "DCC")));

        mockMvc.perform(get("/api/posts/" + postId).header("Authorization", buyer))
                .andExpect(jsonPath("$.requestedByViewer").value(true));
        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(jsonPath("$.ownedByViewer").value(false))
                .andExpect(jsonPath("$.requestedByViewer").value(false));
    }

    @Test
    @DisplayName("Seller and buyer can reply on a request; strangers cannot")
    void repliesBetweenSellerAndBuyer() throws Exception {
        String postId = createPost();
        mockMvc.perform(post("/api/posts/" + postId + "/interests").header("Authorization", buyer)
                .contentType(MediaType.APPLICATION_JSON).content(requestBody(Instant.now().plus(1, ChronoUnit.DAYS), "Union")));
        String interestId = null;
        for (JsonNode p : myPosts(seller)) {
            if (p.get("id").asText().equals(postId)) interestId = p.get("inquiries").get(0).get("id").asText();
        }

        mockMvc.perform(post("/api/interests/" + interestId + "/replies").header("Authorization", seller)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"Friday 5pm works!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorRole").value("SELLER"));
        mockMvc.perform(post("/api/interests/" + interestId + "/replies").header("Authorization", buyer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"See you then\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorRole").value("BUYER"));
        mockMvc.perform(post("/api/interests/" + interestId + "/replies").header("Authorization", stranger)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"me too\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/users/me/interests").header("Authorization", buyer))
                .andExpect(jsonPath("$[0].sellerName").value("Sally Seller"))
                .andExpect(jsonPath("$[0].pickupPlace").value("Union"))
                .andExpect(jsonPath("$[0].replies[0].message").value("Friday 5pm works!"))
                .andExpect(jsonPath("$[0].replies[1].authorRole").value("BUYER"));
    }

    @Test
    @DisplayName("Requests need sign-in and a future pickup time")
    void validation() throws Exception {
        String postId = createPost();
        mockMvc.perform(post("/api/posts/" + postId + "/interests")
                        .contentType(MediaType.APPLICATION_JSON).content(requestBody(Instant.now().plus(1, ChronoUnit.DAYS), "Union")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/posts/" + postId + "/interests").header("Authorization", buyer)
                        .contentType(MediaType.APPLICATION_JSON).content(requestBody(Instant.now().minus(1, ChronoUnit.HOURS), "Union")))
                .andExpect(status().isBadRequest());
    }
}
