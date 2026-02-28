package cz.phsoft.hokej.user.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že uživatel nemá oprávnění pracovat s daným hráčem.
 *
 * Vyhazuje se, pokud se přihlášený uživatel snaží číst nebo měnit
 * data hráče, který mu nepatří (není k němu přiřazen).
 *
 * Typicky mapováno na HTTP 403 (Forbidden).
 */
public class ForbiddenPlayerAccessException extends BusinessException {

    public ForbiddenPlayerAccessException(Long playerId) {
        super("BE - Hráč " + playerId + " nepatří přihlášenému uživateli.", HttpStatus.FORBIDDEN);
    }
}
