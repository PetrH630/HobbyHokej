package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidMatchStatusException extends BusinessException {
    public InvalidMatchStatusException(Long id, String message) {
        super("BE - Zápas s id: " + id + message, HttpStatus.CONFLICT);
    }


}
