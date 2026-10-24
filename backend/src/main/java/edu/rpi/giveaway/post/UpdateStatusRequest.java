package edu.rpi.giveaway.post;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "Status is required")
        PostStatus status
) {
}
