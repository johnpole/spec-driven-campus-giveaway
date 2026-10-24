package edu.rpi.giveaway.media;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MediaAutofillTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MediaService mediaService;

    @Test
    @DisplayName("When AI auto-fill is disabled, POST /api/media/autofill returns 403 Forbidden")
    void autoFillDisabledReturns403() throws Exception {
        mediaService.setAutofillEnabled(false);

        mockMvc.perform(post("/api/media/autofill").header("Authorization", "Bearer test-token:u-student:student@rpi.edu:Student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"photoUrl\":\"/api/media/desk.jpg\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("AI Auto-Fill is currently disabled"));
    }

    @Test
    @DisplayName("When AI auto-fill is enabled, POST /api/media/autofill returns structured suggestions")
    void autoFillEnabledReturnsSuggestions() throws Exception {
        mediaService.setAutofillEnabled(true);

        mockMvc.perform(post("/api/media/autofill").header("Authorization", "Bearer test-token:u-student:student@rpi.edu:Student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"photoUrl\":\"/api/media/calc-book.jpg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Textbooks"))
                .andExpect(jsonPath("$.condition").value("LIKE_NEW"))
                .andExpect(jsonPath("$.suggestedPrice").value(20.0));
    }

    @Test
    @DisplayName("GET /api/media/autofill/status reports feature flag state and toggle changes state")
    void toggleChangesFeatureState() throws Exception {
        mockMvc.perform(post("/api/media/autofill/toggle?enabled=true")
                        .header("Authorization", "Bearer test-token:admin:admin@campus-giveaway.test:John Pole"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));

        mockMvc.perform(get("/api/media/autofill/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));

        mockMvc.perform(post("/api/media/autofill/toggle?enabled=false")
                        .header("Authorization", "Bearer test-token:admin:admin@campus-giveaway.test:John Pole"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @DisplayName("When Gemini API key is configured but invalid, autoFill gracefully falls back to presentation heuristics")
    void geminiFailureFallsBackToHeuristics() throws Exception {
        mediaService.setAutofillEnabled(true);
        mediaService.setGeminiApiKey("invalid-key-test");

        mockMvc.perform(post("/api/media/autofill").header("Authorization", "Bearer test-token:u-student:student@rpi.edu:Student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"photoUrl\":\"/api/media/wooden-desk.jpg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Furniture"))
                .andExpect(jsonPath("$.condition").value("GOOD"));
    }
}
