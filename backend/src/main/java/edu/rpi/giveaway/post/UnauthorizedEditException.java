package edu.rpi.giveaway.post;

public class UnauthorizedEditException extends RuntimeException {
    public UnauthorizedEditException(String message) {
        super(message);
    }
}
