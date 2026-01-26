package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidChangePlayerUserException extends BusinessException {

    public InvalidChangePlayerUserException() {
        super("Hráč už je přířazen tomuto uživateli", HttpStatus.CONFLICT);
    }
}
