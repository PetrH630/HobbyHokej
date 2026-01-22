package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidPlayerStatusException extends BusinessException {
    public InvalidPlayerStatusException(String message) {
        super(message, HttpStatus.BAD_REQUEST); // 400
    }
}