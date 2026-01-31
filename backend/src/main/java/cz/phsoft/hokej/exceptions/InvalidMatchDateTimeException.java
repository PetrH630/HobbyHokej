package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidMatchDateTimeException extends BusinessException {
    public InvalidMatchDateTimeException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}