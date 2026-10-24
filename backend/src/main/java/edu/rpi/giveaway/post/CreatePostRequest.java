package edu.rpi.giveaway.post;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public record CreatePostRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title cannot exceed 100 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @NotNull(message = "Category is required")
        Category category,

        @NotNull(message = "Condition is required")
        Condition condition,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.00", message = "Price must be zero or positive")
        BigDecimal price,

        @NotBlank(message = "Pickup location is required")
        String pickupLocation,

        @NotNull(message = "Available until date is required")
        @Future(message = "Available until date must be in the future")
        Instant availableUntil,

        @NotBlank(message = "Photo URL is required")
        String photoUrl,

        @NotBlank(message = "Poster name is required")
        String posterName,

        @NotBlank(message = "Poster contact is required")
        @Pattern(
                regexp = "^([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|\\+[1-9]\\d{1,14})$",
                message = "Poster contact must be a valid email or E.164 phone number"
        )
        String posterContact,

        @NotNull(message = "Legal confirmation is required")
        @AssertTrue(message = "Must confirm legal, safe, and mine to give")
        Boolean legalConfirmed
) {
    public boolean isFree() {
        return price != null && price.compareTo(BigDecimal.ZERO) == 0;
    }
}
