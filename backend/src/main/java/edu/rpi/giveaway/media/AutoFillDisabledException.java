package edu.rpi.giveaway.media;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AutoFillDisabledException extends RuntimeException {
    public AutoFillDisabledException(String message) {
        super(message);
    }
}
