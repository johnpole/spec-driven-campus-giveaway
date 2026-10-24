package edu.rpi.giveaway.auth;

public record UserPrincipal(
        String userId,
        String email,
        String name,
        String pictureUrl,
        boolean isAdmin
) {
    public UserPrincipal(String userId, String email, String name, String pictureUrl) {
        this(userId, email, name, pictureUrl, false);
    }
}
