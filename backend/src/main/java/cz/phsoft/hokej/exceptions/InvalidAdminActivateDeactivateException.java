package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidAdminActivateDeactivateException extends BusinessException {
    public InvalidAdminActivateDeactivateException(String message) {

        super(message, HttpStatus.METHOD_NOT_ALLOWED);
    }
}
