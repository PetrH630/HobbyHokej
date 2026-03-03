package cz.phsoft.hokej.user.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že uživatel již existuje.
 *
 * Typicky se používá při registraci, pokud je e-mail
 * nebo jiný identifikátor uživatele již obsazen.
 *
 * Výjimka je mapována na HTTP status CONFLICT.
 */
public class UserAlreadyExistsException extends BusinessException {

    /**
     * Vytvoří výjimku s detailní chybovou zprávou.
     *
     * @param message popis důvodu, proč nelze uživatele vytvořit
     */
    public UserAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}