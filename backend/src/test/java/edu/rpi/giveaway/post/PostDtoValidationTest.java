package edu.rpi.giveaway.post;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PostDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private CreatePostRequest createValidRequest() {
        return new CreatePostRequest(
                "Mini Fridge 3.2 cu ft",
                "Works great, clean, moving out of Quad dorm.",
                Category.DORM_ESSENTIALS,
                Condition.GOOD,
                new BigDecimal("25.00"),
                "Quad Hall",
                Instant.now().plus(7, ChronoUnit.DAYS),
                "/api/media/photo-123.jpg",
                "Alex Chen",
                "chena@rpi.edu",
                true
        );
    }

    @Test
    void validRequestPassesValidation() {
        CreatePostRequest request = createValidRequest();
        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void validE164PhonePassesValidation() {
        CreatePostRequest request = new CreatePostRequest(
                "Mini Fridge",
                "Clean",
                Category.DORM_ESSENTIALS,
                Condition.GOOD,
                BigDecimal.ZERO,
                "Barton Hall",
                Instant.now().plus(2, ChronoUnit.DAYS),
                "/api/media/photo.jpg",
                "Alex Chen",
                "+15185550199",
                true
        );
        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void malformedContactFailsValidation() {
        CreatePostRequest request = new CreatePostRequest(
                "Mini Fridge",
                "Clean",
                Category.DORM_ESSENTIALS,
                Condition.GOOD,
                BigDecimal.ZERO,
                "Barton Hall",
                Instant.now().plus(2, ChronoUnit.DAYS),
                "/api/media/photo.jpg",
                "Alex Chen",
                "not-an-email-or-phone",
                true
        );
        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("posterContact"));
    }

    @Test
    void titleExceeding100CharsFailsValidation() {
        String longTitle = "A".repeat(101);
        CreatePostRequest request = new CreatePostRequest(
                longTitle,
                "Description",
                Category.FURNITURE,
                Condition.FAIR,
                BigDecimal.ZERO,
                "Commons",
                Instant.now().plus(2, ChronoUnit.DAYS),
                "/api/media/p.jpg",
                "Sam",
                "sam@rpi.edu",
                true
        );
        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
    }

    @Test
    void descriptionExceeding1000CharsFailsValidation() {
        String longDesc = "B".repeat(1001);
        CreatePostRequest request = new CreatePostRequest(
                "Desk",
                longDesc,
                Category.FURNITURE,
                Condition.FAIR,
                BigDecimal.ZERO,
                "Commons",
                Instant.now().plus(2, ChronoUnit.DAYS),
                "/api/media/p.jpg",
                "Sam",
                "sam@rpi.edu",
                true
        );
        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("description"));
    }

    @Test
    void negativePriceFailsValidation() {
        CreatePostRequest request = new CreatePostRequest(
                "Desk",
                "Wooden desk",
                Category.FURNITURE,
                Condition.FAIR,
                new BigDecimal("-5.00"),
                "Commons",
                Instant.now().plus(2, ChronoUnit.DAYS),
                "/api/media/p.jpg",
                "Sam",
                "sam@rpi.edu",
                true
        );
        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("price"));
    }

    @Test
    void pastAvailableUntilDateFailsValidation() {
        CreatePostRequest request = new CreatePostRequest(
                "Desk",
                "Wooden desk",
                Category.FURNITURE,
                Condition.FAIR,
                BigDecimal.ZERO,
                "Commons",
                Instant.now().minus(2, ChronoUnit.DAYS),
                "/api/media/p.jpg",
                "Sam",
                "sam@rpi.edu",
                true
        );
        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("availableUntil"));
    }

    @Test
    void unconfirmedLegalStatementFailsValidation() {
        CreatePostRequest request = new CreatePostRequest(
                "Desk",
                "Wooden desk",
                Category.FURNITURE,
                Condition.FAIR,
                BigDecimal.ZERO,
                "Commons",
                Instant.now().plus(2, ChronoUnit.DAYS),
                "/api/media/p.jpg",
                "Sam",
                "sam@rpi.edu",
                false
        );
        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("legalConfirmed"));
    }

    @Test
    void postDetailResponseStripsPrivateContactAndToken() {
        PostDocument doc = new PostDocument();
        doc.setId("item-1");
        doc.setTitle("Chemistry Textbook");
        doc.setDescription("Chem I textbook");
        doc.setCategory("Textbooks");
        doc.setCondition("LIKE_NEW");
        doc.setPrice(0.0);
        doc.setIsFree(true);
        doc.setPickupLocation("Folsom Library");
        doc.setAvailableUntil(Date.from(Instant.now().plus(5, ChronoUnit.DAYS)));
        doc.setPhotoUrl("/api/media/book.jpg");
        doc.setStatus("AVAILABLE");
        doc.setPosterName("Jordan");
        doc.setPosterContact("private-email@rpi.edu");
        doc.setEditTokenHash("secret-sha256-hash");
        doc.setInterestCount(2);
        doc.setCreatedAt(new Date());

        PostDetailResponse response = PostDetailResponse.fromDocument(doc);

        assertThat(response.id()).isEqualTo("item-1");
        assertThat(response.title()).isEqualTo("Chemistry Textbook");
        assertThat(response.posterName()).isEqualTo("Jordan");
        // Verify class does not even have posterContact or editTokenHash fields
        assertThat(PostDetailResponse.class.getDeclaredFields())
                .noneMatch(f -> f.getName().equals("posterContact") || f.getName().equals("editTokenHash"));
    }

    @Test
    void tokenUtilsHashesDeterministicallyAndVerifies() {
        String token = "raw-secret-token-12345";
        String hash = TokenUtils.hashToken(token);

        assertThat(hash).isNotNull().hasSize(64);
        assertThat(TokenUtils.verifyToken(token, hash)).isTrue();
        assertThat(TokenUtils.verifyToken("wrong-token", hash)).isFalse();
    }
}
