package edu.rpi.giveaway.auth;

public record AuthResponse(
        String token,
        AuthUserDto user
) {}
