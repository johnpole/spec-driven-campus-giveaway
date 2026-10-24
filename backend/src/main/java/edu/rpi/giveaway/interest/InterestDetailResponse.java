package edu.rpi.giveaway.interest;

import java.time.Instant;
import java.util.List;

/** A request as the poster sees it: who, how to reach them, when and where, and the conversation. */
public record InterestDetailResponse(
        String id,
        String visitorName,
        String visitorContact,
        String message,
        Instant createdAt,
        Instant pickupAt,
        String pickupPlace,
        List<InterestReplyResponse> replies
) {
    public InterestDetailResponse(String id, String visitorName, String visitorContact, String message, Instant createdAt) {
        this(id, visitorName, visitorContact, message, createdAt, null, null, List.of());
    }

    public static InterestDetailResponse fromDocument(InterestDocument doc) {
        return fromDocument(doc, List.of());
    }

    public static InterestDetailResponse fromDocument(InterestDocument doc, List<InterestReplyResponse> replies) {
        return new InterestDetailResponse(
                doc.getId(),
                doc.getVisitorName(),
                doc.getVisitorContact(),
                doc.getMessage(),
                doc.getCreatedAt() != null ? doc.getCreatedAt().toInstant() : null,
                doc.getPickupAt() != null ? doc.getPickupAt().toInstant() : null,
                doc.getPickupPlace(),
                replies
        );
    }
}
