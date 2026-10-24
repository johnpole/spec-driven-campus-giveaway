package edu.rpi.giveaway.post;

import edu.rpi.giveaway.auth.UserContext;
import edu.rpi.giveaway.auth.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostDetailStatusDeleteIntegrationTest {

    private PostRepository postRepository;
    private PostService postService;
    private String rawEditToken;
    private String postId;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentUser(new UserPrincipal("author-123", "leej@rpi.edu", "Jordan Lee", null));
        postRepository = new FirestorePostRepository(null);
        postService = new PostService(postRepository, "campus-giveaway-photos");

        CreatePostRequest request = new CreatePostRequest(
                "Calculus Early Transcendentals",
                "Math textbook, good condition",
                Category.TEXTBOOKS,
                Condition.GOOD,
                new BigDecimal("15.00"),
                "Folsom Library 1st Floor",
                Instant.now().plus(7, ChronoUnit.DAYS),
                "/api/media/book.jpg",
                "Jordan Lee",
                "leej@rpi.edu",
                true
        );

        CreatePostResponse created = postService.createPost(request);
        postId = created.id();
        rawEditToken = created.editToken();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void getDetailReturnsPostAndOmitsPrivateFields() {
        PostDetailResponse detail = postService.getPostDetail(postId);

        assertThat(detail).isNotNull();
        assertThat(detail.id()).isEqualTo(postId);
        assertThat(detail.title()).isEqualTo("Calculus Early Transcendentals");
        assertThat(detail.status()).isEqualTo("AVAILABLE");
        assertThat(detail.price()).isEqualByComparingTo("15.00");
    }

    @Test
    void getDetailOnClaimedItemReturns200WithClaimedStatus() {
        postService.updateStatus(postId, rawEditToken, PostStatus.CLAIMED);

        PostDetailResponse detail = postService.getPostDetail(postId);
        assertThat(detail.status()).isEqualTo("CLAIMED");
    }

    @Test
    void getDetailOnNonExistentItemThrowsNotFound() {
        assertThatThrownBy(() -> postService.getPostDetail("non-existent-id"))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void updateStatusWithValidTokenSucceeds() {
        PostDetailResponse updated = postService.updateStatus(postId, rawEditToken, PostStatus.CLAIMED);
        assertThat(updated.status()).isEqualTo("CLAIMED");

        PostDocument inRepo = postRepository.findById(postId).orElseThrow();
        assertThat(inRepo.getStatus()).isEqualTo("CLAIMED");
    }

    @Test
    void updateStatusWithInvalidTokenThrowsUnauthorized() {
        UserContext.clear();
        assertThatThrownBy(() -> postService.updateStatus(postId, "wrong-token-abc", PostStatus.CLAIMED))
                .isInstanceOf(UnauthorizedEditException.class);
    }

    @Test
    void deletePostWithValidTokenRemovesItem() {
        postService.deletePost(postId, rawEditToken);

        assertThat(postRepository.findById(postId)).isEmpty();
        assertThatThrownBy(() -> postService.getPostDetail(postId))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void deletePostWithInvalidTokenThrowsUnauthorized() {
        UserContext.clear();
        assertThatThrownBy(() -> postService.deletePost(postId, "wrong-token-xyz"))
                .isInstanceOf(UnauthorizedEditException.class);
        assertThat(postRepository.findById(postId)).isPresent();
    }
}
