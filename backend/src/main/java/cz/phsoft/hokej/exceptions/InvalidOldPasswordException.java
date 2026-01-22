package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidOldPasswordException extends BusinessException {

    public InvalidOldPasswordException() {
        super("BE - Staré heslo je nesprávné", HttpStatus.BAD_REQUEST);
    }
}