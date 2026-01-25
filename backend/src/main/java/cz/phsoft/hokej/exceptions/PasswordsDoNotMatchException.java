package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že zadaná hesla se neshodují.
 *
 * <p>
 * Používá se při registraci nebo změně hesla, pokud nové heslo
 * a jeho potvrzení nejsou stejné.
 * </p>
 *
 * Typicky mapováno na HTTP 400 (Bad Request).
 */
public class PasswordsDoNotMatchException extends BusinessException {

    public PasswordsDoNotMatchException() {
        super("BE - Hesla se neshodují.", HttpStatus.BAD_REQUEST);
    }

    public PasswordsDoNotMatchException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
