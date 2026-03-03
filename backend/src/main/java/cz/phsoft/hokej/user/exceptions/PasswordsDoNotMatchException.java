package cz.phsoft.hokej.user.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že zadaná hesla se neshodují.
 *
 * Používá se při registraci nebo změně hesla, pokud nové heslo
 * a jeho potvrzení nejsou stejné.
 *
 * Výjimka je mapována na HTTP status BAD_REQUEST.
 */
public class PasswordsDoNotMatchException extends BusinessException {

    /**
     * Vytvoří výjimku s předdefinovanou chybovou zprávou.
     */
    public PasswordsDoNotMatchException() {
        super("BE - Hesla se neshodují.", HttpStatus.BAD_REQUEST);
    }

    /**
     * Alternativní konstruktor pro vlastní chybovou zprávu.
     *
     * @param message vlastní chybová zpráva
     */
    public PasswordsDoNotMatchException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}