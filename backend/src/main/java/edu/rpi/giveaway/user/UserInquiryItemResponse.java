package edu.rpi.giveaway.user;

import edu.rpi.giveaway.interest.InterestReplyResponse;

import java.time.Instant;
import java.util.List;

/** A request as the interested student sees it, including the poster's replies. */
public record UserInquiryItemResponse(
        String id,
        String postId,
        String itemTitle,
        String itemPhotoUrl,
        String itemStatus,
        String message,
        Instant createdAt,
        String sellerName,
        Instant pickupAt,
        String pickupPlace,
        List<InterestReplyResponse> replies
) {
}
