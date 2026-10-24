package edu.rpi.giveaway.interest;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.rpi.giveaway.post.GlobalExceptionHandler;
import edu.rpi.giveaway.post.UnauthorizedEditException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InterestController.class)
@Import(GlobalExceptionHandler.class)
class InterestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InterestService interestService;

    @Test
    void submitInterestReturns201Created() throws Exception {
        CreateInterestRequest request = new CreateInterestRequest(
                "Taylor Jordan",
                "jordat@rpi.edu",
                "I can pick this up tomorrow after class!"
        );

        doNothing().when(interestService).submitInterest(eq("item-1"), any(CreateInterestRequest.class));

        mockMvc.perform(post("/api/posts/item-1/interests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void submitInterestOnClaimedPostReturns409Conflict() throws Exception {
        CreateInterestRequest request = new CreateInterestRequest(
                "Taylor Jordan",
                "jordat@rpi.edu",
                "Can I have this?"
        );

        doThrow(new ItemNotAvailableException("Item is already claimed and no longer available"))
                .when(interestService).submitInterest(eq("item-1"), any(CreateInterestRequest.class));

        mockMvc.perform(post("/api/posts/item-1/interests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Item is already claimed and no longer available"));
    }

    @Test
    void submitInterestWithInvalidContactReturns400BadRequest() throws Exception {
        CreateInterestRequest request = new CreateInterestRequest(
                "Taylor Jordan",
                "invalid-contact-info",
                "Hello"
        );

        mockMvc.perform(post("/api/posts/item-1/interests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void getInterestSummaryReturns200WithMaskedNames() throws Exception {
        InterestSummaryResponse summary = new InterestSummaryResponse(
                3,
                List.of("Taylor", "Jordan", "Sam")
        );

        when(interestService.getInterestSummary("item-1")).thenReturn(summary);

        mockMvc.perform(get("/api/posts/item-1/interests/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(3))
                .andExpect(jsonPath("$.firstNames[0]").value("Taylor"))
                .andExpect(jsonPath("$.firstNames[1]").value("Jordan"))
                .andExpect(jsonPath("$.firstNames[2]").value("Sam"))
                .andExpect(jsonPath("$.visitorContact").doesNotExist())
                .andExpect(jsonPath("$.message").doesNotExist());
    }

    @Test
    void getPrivateInterestsWithValidTokenReturns200WithFullInquiries() throws Exception {
        InterestDetailResponse detail = new InterestDetailResponse(
                "interest-1",
                "Taylor Jordan",
                "jordat@rpi.edu",
                "Pick up at 4pm",
                Instant.now()
        );

        when(interestService.getPrivateInterests("item-1", "secret-token"))
                .thenReturn(List.of(detail));

        mockMvc.perform(get("/api/posts/item-1/interests")
                        .header("X-Edit-Token", "secret-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].visitorName").value("Taylor Jordan"))
                .andExpect(jsonPath("$[0].visitorContact").value("jordat@rpi.edu"))
                .andExpect(jsonPath("$[0].message").value("Pick up at 4pm"));
    }

    @Test
    void getPrivateInterestsWithoutTokenReturns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/posts/item-1/interests"))
                .andExpect(status().isForbidden());
    }
}
