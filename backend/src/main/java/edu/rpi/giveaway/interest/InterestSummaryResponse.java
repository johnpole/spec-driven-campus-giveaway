package edu.rpi.giveaway.interest;

import java.util.List;

public record InterestSummaryResponse(
        int totalCount,
        List<String> firstNames
) {
}
