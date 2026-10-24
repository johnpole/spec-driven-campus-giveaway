package edu.rpi.giveaway.post;

import java.math.BigDecimal;
import java.time.Instant;

public record PostDetailResponse(
        String id,
        String title,
        String description,
        String category,
        String condition,
        BigDecimal price,
        boolean isFree,
        String pickupLocation,
        Instant availableUntil,
        String photoUrl,
        String status,
        String posterName,
        int interestCount,
        Instant createdAt
) {
    public static PostDetailResponse fromDocument(PostDocument doc) {
        return new PostDetailResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getDescription(),
                doc.getCategory(),
                doc.getCondition(),
                doc.getPrice() != null ? BigDecimal.valueOf(doc.getPrice()) : BigDecimal.ZERO,
                Boolean.TRUE.equals(doc.getIsFree()),
                doc.getPickupLocation(),
                doc.getAvailableUntil() != null ? doc.getAvailableUntil().toInstant() : null,
                doc.getPhotoUrl(),
                doc.getStatus(),
                doc.getPosterName(),
                doc.getInterestCount() != null ? doc.getInterestCount() : 0,
                doc.getCreatedAt() != null ? doc.getCreatedAt().toInstant() : null
        );
    }
}
