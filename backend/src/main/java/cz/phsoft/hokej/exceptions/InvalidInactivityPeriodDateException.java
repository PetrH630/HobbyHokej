package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidInactivityPeriodDateException extends BusinessException {
    public InvalidInactivityPeriodDateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}