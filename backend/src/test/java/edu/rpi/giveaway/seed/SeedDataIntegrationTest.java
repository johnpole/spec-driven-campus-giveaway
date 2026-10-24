package edu.rpi.giveaway.seed;

import edu.rpi.giveaway.post.PostDocument;
import edu.rpi.giveaway.post.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "giveaway.seed.enabled=true",
        "spring.cloud.gcp.firestore.enabled=false"
})
class SeedDataIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("Startup seed initializer populates exactly 12 realistic RPI campus posts")
    void seedsTwelveRealisticPosts() {
        List<PostDocument> allPosts = postRepository.findAll();
        assertThat(allPosts).hasSize(12);

        // Verify representative categories and items are present
        assertThat(allPosts)
                .extracting(PostDocument::getTitle)
                .contains(
                        "IKEA Micke Desk - White",
                        "Dell 27-inch 144Hz Gaming Monitor",
                        "Stewart Calculus: Early Transcendentals 9th Ed",
                        "Twin XL Memory Foam Mattress Topper (3-inch)",
                        "Honeywell QuietSet Tower Fan"
                );

        // Verify all 12 posts have valid active status and non-null pickup locations
        assertThat(allPosts).allSatisfy(post -> {
            assertThat(post.getStatus()).isEqualTo("AVAILABLE");
            assertThat(post.getPickupLocation()).isNotBlank();
            assertThat(post.getPhotoUrl()).isNotBlank();
            assertThat(post.getEditTokenHash()).isNotBlank();
            assertThat(post.getPosterContact()).isNotBlank();
        });
    }
}
