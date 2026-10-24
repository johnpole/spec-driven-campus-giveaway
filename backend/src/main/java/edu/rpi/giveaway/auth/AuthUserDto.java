package edu.rpi.giveaway.auth;

public record AuthUserDto(
        String id,
        String email,
        String name,
        String firstName,
        String lastName,
        String phoneNumber,
        boolean isAdmin
) {}
