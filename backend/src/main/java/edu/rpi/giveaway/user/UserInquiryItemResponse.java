package edu.rpi.giveaway.user;

import java.time.Instant;

public record UserInquiryItemResponse(
        String id,
        String postId,
        String itemTitle,
        String itemPhotoUrl,
        String itemStatus,
        String message,
        Instant createdAt
) {
}
