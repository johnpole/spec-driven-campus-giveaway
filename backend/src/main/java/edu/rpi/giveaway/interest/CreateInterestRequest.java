package edu.rpi.giveaway.interest;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateInterestRequest(
        @JsonAlias("name")
        @Size(max = 100, message = "Visitor name cannot exceed 100 characters")
        String visitorName,

        @JsonAlias("contact")
        @Pattern(
                regexp = "^$|^([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|\\+[1-9]\\d{1,14})$",
                message = "Visitor contact must be a valid email or E.164 phone number"
        )
        @Size(max = 100, message = "Visitor contact cannot exceed 100 characters")
        String visitorContact,

        @Size(max = 500, message = "Message cannot exceed 500 characters")
        String message,

        /** When the buyer proposes to pick the item up. */
        java.time.Instant pickupAt,

        @Size(max = 150, message = "Pickup place cannot exceed 150 characters")
        String pickupPlace
) {
    public CreateInterestRequest(String visitorName, String visitorContact, String message) {
        this(visitorName, visitorContact, message, null, null);
    }
}
