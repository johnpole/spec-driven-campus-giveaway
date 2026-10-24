package edu.rpi.giveaway.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PostFeedAndSearchIntegrationTest {

    private PostRepository postRepository;
    private PostService postService;

    @BeforeEach
    void setUp() {
        postRepository = new FirestorePostRepository(null);
        postService = new PostService(postRepository, "campus-giveaway-photos");

        // Seed 1: Active Electronics ($50)
        PostDocument p1 = new PostDocument();
        p1.setId("item-1");
        p1.setTitle("Dell 27-inch Monitor");
        p1.setDescription("4K IPS monitor with stand");
        p1.setCategory("Electronics");
        p1.setCondition("LIKE_NEW");
        p1.setPrice(50.0);
        p1.setIsFree(false);
        p1.setPickupLocation("DCC 308");
        p1.setAvailableUntil(Date.from(Instant.now().plus(5, ChronoUnit.DAYS)));
        p1.setPhotoUrl("/api/media/monitor.jpg");
        p1.setStatus("AVAILABLE");
        p1.setPosterName("Carlos");
        p1.setCreatedAt(Date.from(Instant.now().minus(2, ChronoUnit.HOURS)));
        postRepository.save(p1);

        // Seed 2: Active Free Furniture
        PostDocument p2 = new PostDocument();
        p2.setId("item-2");
        p2.setTitle("Wooden Bookshelf");
        p2.setDescription("Sturdy 3-shelf wooden bookcase, pickup in Quad");
        p2.setCategory("Furniture");
        p2.setCondition("GOOD");
        p2.setPrice(0.0);
        p2.setIsFree(true);
        p2.setPickupLocation("Quad Hall");
        p2.setAvailableUntil(Date.from(Instant.now().plus(3, ChronoUnit.DAYS)));
        p2.setPhotoUrl("/api/media/shelf.jpg");
        p2.setStatus("AVAILABLE");
        p2.setPosterName("Alex");
        p2.setCreatedAt(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)));
        postRepository.save(p2);

        // Seed 3: CLAIMED Furniture (should be excluded from active feed)
        PostDocument p3 = new PostDocument();
        p3.setId("item-3");
        p3.setTitle("Comfy Armchair");
        p3.setDescription("Blue armchair");
        p3.setCategory("Furniture");
        p3.setCondition("GOOD");
        p3.setPrice(0.0);
        p3.setIsFree(true);
        p3.setPickupLocation("Barton");
        p3.setAvailableUntil(Date.from(Instant.now().plus(5, ChronoUnit.DAYS)));
        p3.setPhotoUrl("/api/media/chair.jpg");
        p3.setStatus("CLAIMED");
        p3.setPosterName("Priya");
        p3.setCreatedAt(Date.from(Instant.now().minus(3, ChronoUnit.HOURS)));
        postRepository.save(p3);

        // Seed 4: Expired Item (availableUntil in past)
        PostDocument p4 = new PostDocument();
        p4.setId("item-4");
        p4.setTitle("Old Toaster");
        p4.setDescription("Working 2-slice toaster");
        p4.setCategory("Kitchen");
        p4.setCondition("FAIR");
        p4.setPrice(0.0);
        p4.setIsFree(true);
        p4.setPickupLocation("RAH");
        p4.setAvailableUntil(Date.from(Instant.now().minus(1, ChronoUnit.DAYS)));
        p4.setPhotoUrl("/api/media/toaster.jpg");
        p4.setStatus("AVAILABLE");
        p4.setPosterName("Jordan");
        p4.setCreatedAt(Date.from(Instant.now().minus(5, ChronoUnit.DAYS)));
        postRepository.save(p4);
    }

    @Test
    void activeFeedExcludesClaimedAndExpiredItems() {
        List<PostSummaryResponse> feed = postService.getActiveFeed(null, false, null);

        assertThat(feed).hasSize(2);
        assertThat(feed).extracting(PostSummaryResponse::id).containsExactly("item-2", "item-1"); // sorted newest first
    }

    @Test
    void activeFeedFiltersByCategory() {
        List<PostSummaryResponse> feed = postService.getActiveFeed("Electronics", false, null);

        assertThat(feed).hasSize(1);
        assertThat(feed.get(0).title()).isEqualTo("Dell 27-inch Monitor");
    }

    @Test
    void activeFeedFiltersByFreeOnly() {
        List<PostSummaryResponse> feed = postService.getActiveFeed(null, true, null);

        assertThat(feed).hasSize(1);
        assertThat(feed.get(0).title()).isEqualTo("Wooden Bookshelf");
        assertThat(feed.get(0).isFree()).isTrue();
    }

    @Test
    void keywordSearchMatchesTitleAndDescription() {
        List<PostSummaryResponse> byTitle = postService.getActiveFeed(null, false, "monitor");
        assertThat(byTitle).hasSize(1);
        assertThat(byTitle.get(0).id()).isEqualTo("item-1");

        List<PostSummaryResponse> byDesc = postService.getActiveFeed(null, false, "bookcase");
        assertThat(byDesc).hasSize(1);
        assertThat(byDesc.get(0).id()).isEqualTo("item-2");

        List<PostSummaryResponse> noMatch = postService.getActiveFeed(null, false, "nonexistentitem");
        assertThat(noMatch).isEmpty();
    }
}
