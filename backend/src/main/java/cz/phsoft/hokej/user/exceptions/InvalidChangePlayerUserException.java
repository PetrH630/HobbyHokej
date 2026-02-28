package cz.phsoft.hokej.user.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatný pokus o změnu přiřazení hráče k uživateli.
 *
 * Vyhazuje se v případě, kdy se aplikace pokusí přiřadit hráče
 * ke stejnému uživateli, ke kterému již patří.
 *
 * Typicky mapováno na HTTP 409 (Conflict).
 */
public class InvalidChangePlayerUserException extends BusinessException {

    /**
     * Vytvoří výjimku s předdefinovanou chybovou zprávou.
     */
    public InvalidChangePlayerUserException() {
        super("Hráč už je přiřazen tomuto uživateli", HttpStatus.CONFLICT);
    }
}
