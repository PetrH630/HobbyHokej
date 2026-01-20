package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT); // 409
    }
}
