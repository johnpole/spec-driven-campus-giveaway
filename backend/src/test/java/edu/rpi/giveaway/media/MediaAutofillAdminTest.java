package edu.rpi.giveaway.media;

import edu.rpi.giveaway.auth.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MediaAutofillAdminTest {

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("Non-admin user cannot toggle AI autofill feature flag")
    void nonAdminCannotToggleAutofill() throws Exception {
        mockMvc.perform(post("/api/media/autofill/toggle")
                        .header("Authorization", "Bearer test-token:regular-user:student@rpi.edu:Student"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin user can toggle AI autofill feature flag")
    void adminCanToggleAutofill() throws Exception {
        mockMvc.perform(post("/api/media/autofill/toggle")
                        .param("enabled", "true")
                        .header("Authorization", "Bearer test-token:admin-id:admin@campus-giveaway.test:John Pole"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }
}
