package edu.rpi.giveaway.media;

public record AutoFillSuggestionResponse(
        String title,
        String category,
        String condition,
        Double suggestedPrice,
        String description
) {
}
