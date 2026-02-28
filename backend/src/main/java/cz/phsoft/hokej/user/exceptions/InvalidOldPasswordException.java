package cz.phsoft.hokej.user.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující nesprávné původní heslo při změně hesla.
 *
 * Vyhazuje se v případě, kdy uživatel zadá špatné aktuální heslo
 * při pokusu o změnu hesla.
 *
 * Typicky mapováno na HTTP 400 (Bad Request).
 */
public class InvalidOldPasswordException extends BusinessException {

    public InvalidOldPasswordException() {
        super("BE - Staré heslo je nesprávné.", HttpStatus.BAD_REQUEST);
    }
}
