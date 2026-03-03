package cz.phsoft.hokej.user.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující nesprávné původní heslo při změně hesla.
 *
 * Vyhazuje se v případě, kdy uživatel zadá nesprávné aktuální heslo
 * při pokusu o změnu hesla v rámci svého účtu.
 *
 * Výjimka je mapována na HTTP status BAD_REQUEST.
 */
public class InvalidOldPasswordException extends BusinessException {

    /**
     * Vytvoří výjimku s předdefinovanou chybovou zprávou.
     */
    public InvalidOldPasswordException() {
        super("BE - Staré heslo je nesprávné.", HttpStatus.BAD_REQUEST);
    }
}