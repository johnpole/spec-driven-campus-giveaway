package edu.rpi.giveaway.post;

import edu.rpi.giveaway.interest.InterestDocument;
import edu.rpi.giveaway.interest.JpaInterestRepository;
import edu.rpi.giveaway.interest.SpringDataInterestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class JpaRepositoryIntegrationTest {

    @Autowired
    private SpringDataPostRepository springDataPostRepository;

    @Autowired
    private SpringDataInterestRepository springDataInterestRepository;

    private JpaPostRepository postRepository;
    private JpaInterestRepository interestRepository;

    @BeforeEach
    void setUp() {
        postRepository = new JpaPostRepository(springDataPostRepository);
        interestRepository = new JpaInterestRepository(springDataInterestRepository);
    }

    @Test
    @DisplayName("Should save, find, and query active posts, filtering out expired ones")
    void testSaveAndQueryActivePosts() {
        Instant now = Instant.now();

        // 1. Active post (expires in 2 days)
        PostDocument activePost = new PostDocument();
        activePost.setId(UUID.randomUUID().toString());
        activePost.setTitle("Desk Lamp");
        activePost.setDescription("LED desk lamp with USB charging port");
        activePost.setCategory("Furniture");
        activePost.setCondition("Like New");
        activePost.setPrice(0.0);
        activePost.setIsFree(true);
        activePost.setPickupLocation("Quad Dorm Room 102");
        activePost.setAvailableUntil(Date.from(now.plusSeconds(172800)));
        activePost.setPhotoUrl("http://localhost:8080/api/media/lamp.jpg");
        activePost.setLegalConfirmed(true);
        activePost.setStatus("AVAILABLE");
        activePost.setPosterName("Alex Chen");
        activePost.setPosterContact("alex@rpi.edu");
        activePost.setEditTokenHash("token123");
        activePost.setCreatedAt(Date.from(now));
        activePost.setKeywords(List.of("desk", "lamp", "led"));

        postRepository.save(activePost);

        // 2. Expired post (expired 1 day ago)
        PostDocument expiredPost = new PostDocument();
        expiredPost.setId(UUID.randomUUID().toString());
        expiredPost.setTitle("Old Couch");
        expiredPost.setDescription("Free couch");
        expiredPost.setCategory("Furniture");
        expiredPost.setCondition("Fair");
        expiredPost.setPrice(0.0);
        expiredPost.setIsFree(true);
        expiredPost.setPickupLocation("Commons");
        expiredPost.setAvailableUntil(Date.from(now.minusSeconds(86400)));
        expiredPost.setPhotoUrl("http://localhost:8080/api/media/couch.jpg");
        expiredPost.setLegalConfirmed(true);
        expiredPost.setStatus("AVAILABLE");
        expiredPost.setPosterName("Sam");
        expiredPost.setPosterContact("sam@rpi.edu");
        expiredPost.setEditTokenHash("token456");
        expiredPost.setCreatedAt(Date.from(now.minusSeconds(100000)));

        postRepository.save(expiredPost);

        // Query active posts
        List<PostDocument> activePosts = postRepository.findAllActive(now);
        assertThat(activePosts).hasSize(1);
        assertThat(activePosts.get(0).getTitle()).isEqualTo("Desk Lamp");

        // Find by ID
        Optional<PostDocument> retrieved = postRepository.findById(activePost.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getKeywords()).contains("lamp", "led");

        // Save Interest
        InterestDocument interest = new InterestDocument();
        interest.setId(UUID.randomUUID().toString());
        interest.setPostId(activePost.getId());
        interest.setVisitorName("Morgan");
        interest.setVisitorContact("518-555-0100");
        interest.setMessage("Can pick up at 6pm");
        interest.setCreatedAt(new Date());

        interestRepository.save(interest);

        List<InterestDocument> interests = interestRepository.findByPostId(activePost.getId());
        assertThat(interests).hasSize(1);
        assertThat(interests.get(0).getVisitorName()).isEqualTo("Morgan");

        // Delete post
        postRepository.deleteById(activePost.getId());
        assertThat(postRepository.findById(activePost.getId())).isEmpty();
    }
}
