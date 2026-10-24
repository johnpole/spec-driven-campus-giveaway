package edu.rpi.giveaway.post;

import edu.rpi.giveaway.interest.InterestDocument;
import edu.rpi.giveaway.interest.InterestEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostEntityTest {

    @Test
    @DisplayName("Should faithfully map PostDocument to PostEntity and back to PostDocument")
    void testPostDocumentMapping() {
        String id = UUID.randomUUID().toString();
        Date now = new Date();
        Date until = new Date(System.currentTimeMillis() + 86400000L);

        PostDocument doc = new PostDocument();
        doc.setId(id);
        doc.setTitle("Microwave Oven");
        doc.setDescription("Works great, 700W");
        doc.setCategory("Appliances");
        doc.setCondition("Good");
        doc.setPrice(0.0);
        doc.setIsFree(true);
        doc.setPickupLocation("Barton Hall Lobby");
        doc.setAvailableUntil(until);
        doc.setPhotoUrl("http://localhost:8080/api/media/test.jpg");
        doc.setLegalConfirmed(true);
        doc.setStatus("AVAILABLE");
        doc.setPosterName("Jane Doe");
        doc.setPosterContact("jane@rpi.edu");
        doc.setEditTokenHash("hash12345");
        doc.setInterestCount(3);
        doc.setCreatedAt(now);
        doc.setKeywords(List.of("microwave", "oven", "700w"));

        PostEntity entity = PostEntity.fromDocument(doc);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getTitle()).isEqualTo("Microwave Oven");
        assertThat(entity.getCategory()).isEqualTo("Appliances");
        assertThat(entity.getCondition()).isEqualTo("Good");
        assertThat(entity.getIsFree()).isTrue();
        assertThat(entity.getPosterContact()).isEqualTo("jane@rpi.edu");
        assertThat(entity.getEditTokenHash()).isEqualTo("hash12345");
        assertThat(entity.getKeywords()).containsExactly("microwave", "oven", "700w");

        PostDocument roundTrip = entity.toDocument();
        assertThat(roundTrip.getId()).isEqualTo(id);
        assertThat(roundTrip.getTitle()).isEqualTo("Microwave Oven");
        assertThat(roundTrip.getInterestCount()).isEqualTo(3);
        assertThat(roundTrip.getKeywords()).containsExactly("microwave", "oven", "700w");
    }

    @Test
    @DisplayName("Should faithfully map InterestDocument to InterestEntity and back")
    void testInterestDocumentMapping() {
        String id = UUID.randomUUID().toString();
        String postId = UUID.randomUUID().toString();
        Date now = new Date();

        InterestDocument doc = new InterestDocument();
        doc.setId(id);
        doc.setPostId(postId);
        doc.setVisitorName("John Smith");
        doc.setVisitorContact("518-555-0199");
        doc.setMessage("I can pick it up today at 5pm!");
        doc.setCreatedAt(now);

        InterestEntity entity = InterestEntity.fromDocument(doc);
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getPostId()).isEqualTo(postId);
        assertThat(entity.getVisitorName()).isEqualTo("John Smith");
        assertThat(entity.getMessage()).isEqualTo("I can pick it up today at 5pm!");

        InterestDocument roundTrip = entity.toDocument();
        assertThat(roundTrip.getId()).isEqualTo(id);
        assertThat(roundTrip.getPostId()).isEqualTo(postId);
        assertThat(roundTrip.getVisitorContact()).isEqualTo("518-555-0199");
    }
}
