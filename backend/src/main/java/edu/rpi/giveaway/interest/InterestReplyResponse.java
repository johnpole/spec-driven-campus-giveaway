package edu.rpi.giveaway.interest;

import java.time.Instant;

public record InterestReplyResponse(
        String id,
        String authorName,
        String authorRole,
        String message,
        Instant createdAt
) {
    public static InterestReplyResponse fromEntity(InterestReplyEntity entity) {
        return new InterestReplyResponse(
                entity.getId(),
                entity.getAuthorName(),
                entity.getAuthorRole(),
                entity.getMessage(),
                entity.getCreatedAt() != null ? entity.getCreatedAt().toInstant() : null);
    }
}
