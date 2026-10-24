package edu.rpi.giveaway.post;

public class InvalidMediaUrlException extends RuntimeException {
    public InvalidMediaUrlException(String message) {
        super(message);
    }
}
