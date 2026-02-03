package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že uživatel již existuje.
 *
 * Typicky se používá při registraci, pokud je e-mail nebo jiný
 * identifikátor uživatele již obsazen.
 *
 * Typicky mapováno na HTTP 409 (Conflict).
 */
public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
