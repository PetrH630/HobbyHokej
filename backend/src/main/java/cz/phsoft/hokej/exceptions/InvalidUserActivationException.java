package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidUserActivationException extends BusinessException {
    public InvalidUserActivationException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
