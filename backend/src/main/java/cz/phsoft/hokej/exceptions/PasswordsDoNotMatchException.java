package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class PasswordsDoNotMatchException extends BusinessException {

    public PasswordsDoNotMatchException() {
        super("BE - Hesla se neshodují", HttpStatus.BAD_REQUEST);
    }

    public PasswordsDoNotMatchException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}