package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class SeasonNotFoundException extends BusinessException {
    public SeasonNotFoundException() {
        super("BE - Sezóna nebyla nalezena.", HttpStatus.CONFLICT);
    }

    public SeasonNotFoundException(String message) {
        super(message, HttpStatus.CONFLICT);
    }





}
