package edu.rpi.giveaway.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostCreationIntegrationTest {

    private PostRepository postRepository;
    private PostService postService;

    @BeforeEach
    void setUp() {
        postRepository = new FirestorePostRepository(null); // uses robust in-memory document store
        postService = new PostService(postRepository, "campus-giveaway-devfest-photos");
        edu.rpi.giveaway.auth.UserContext.setCurrentUser(new edu.rpi.giveaway.auth.UserPrincipal(
                "user-test-id", "reedm@rpi.edu", "Morgan Reed", null
        ));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        edu.rpi.giveaway.auth.UserContext.clear();
    }

    @Test
    void createPostGeneratesRawTokenAndStoresOnlySha256Hash() {
        CreatePostRequest request = new CreatePostRequest(
                "Desk Lamp",
                "LED desk lamp with USB charger",
                Category.DORM_ESSENTIALS,
                Condition.LIKE_NEW,
                BigDecimal.ZERO,
                "Sharp Hall",
                Instant.now().plus(10, ChronoUnit.DAYS),
                "/api/media/lamp.jpg",
                "Morgan Reed",
                "reedm@rpi.edu",
                true
        );

        CreatePostResponse response = postService.createPost(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotBlank();
        assertThat(response.editToken()).isNotBlank();

        // Query the repository to inspect raw stored document
        Optional<PostDocument> savedOpt = postRepository.findById(response.id());
        assertThat(savedOpt).isPresent();

        PostDocument saved = savedOpt.get();
        assertThat(saved.getTitle()).isEqualTo("Desk Lamp");
        assertThat(saved.getStatus()).isEqualTo("AVAILABLE");
        assertThat(saved.getInterestCount()).isZero();
        assertThat(saved.getPosterContact()).isEqualTo("reedm@rpi.edu");

        // The document must store ONLY the SHA-256 hash, NOT the raw token
        assertThat(saved.getEditTokenHash()).isNotNull();
        assertThat(saved.getEditTokenHash()).isNotEqualTo(response.editToken());
        assertThat(saved.getEditTokenHash()).isEqualTo(TokenUtils.hashToken(response.editToken()));
    }

    @Test
    void createPostRejectsUntrustedPhotoUrl() {
        CreatePostRequest request = new CreatePostRequest(
                "Desk Lamp",
                "LED desk lamp",
                Category.DORM_ESSENTIALS,
                Condition.LIKE_NEW,
                BigDecimal.ZERO,
                "Sharp Hall",
                Instant.now().plus(10, ChronoUnit.DAYS),
                "https://malicious-site.com/hacker.jpg",
                "Morgan Reed",
                "reedm@rpi.edu",
                true
        );

        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(InvalidMediaUrlException.class)
                .hasMessageContaining("Photo URL must reference the trusted media endpoint");
    }
}
