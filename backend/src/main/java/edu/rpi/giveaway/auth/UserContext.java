package edu.rpi.giveaway.auth;

public final class UserContext {
    private static final ThreadLocal<UserPrincipal> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {}

    public static void setCurrentUser(UserPrincipal user) {
        CURRENT_USER.set(user);
    }

    public static UserPrincipal getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static UserPrincipal requireCurrentUser() {
        UserPrincipal user = CURRENT_USER.get();
        if (user == null) {
            throw new UnauthorizedException("Authentication is required for this operation");
        }
        return user;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
