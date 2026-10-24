package edu.rpi.giveaway.interest;

import java.time.Instant;

public record InterestDetailResponse(
        String id,
        String visitorName,
        String visitorContact,
        String message,
        Instant createdAt
) {
    public static InterestDetailResponse fromDocument(InterestDocument doc) {
        return new InterestDetailResponse(
                doc.getId(),
                doc.getVisitorName(),
                doc.getVisitorContact(),
                doc.getMessage(),
                doc.getCreatedAt() != null ? doc.getCreatedAt().toInstant() : null
        );
    }
}
