package edu.rpi.giveaway.user;

import edu.rpi.giveaway.interest.InterestDetailResponse;

import java.time.Instant;
import java.util.List;

public record UserPostItemResponse(
        String id,
        String title,
        String description,
        String category,
        String condition,
        Double price,
        Boolean isFree,
        String pickupLocation,
        Instant availableUntil,
        String photoUrl,
        String status,
        int interestCount,
        Instant createdAt,
        List<InterestDetailResponse> inquiries
) {
}
