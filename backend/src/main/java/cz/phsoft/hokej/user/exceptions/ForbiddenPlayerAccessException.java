package cz.phsoft.hokej.user.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že uživatel nemá oprávnění pracovat s daným hráčem.
 *
 * Vyhazuje se, pokud se přihlášený uživatel snaží číst nebo měnit
 * data hráče, který mu nepatří nebo není k němu přiřazen.
 *
 * Výjimka je mapována na HTTP status FORBIDDEN a reprezentuje
 * porušení přístupových pravidel aplikace.
 */
public class ForbiddenPlayerAccessException extends BusinessException {

    /**
     * Vytvoří výjimku s identifikací hráče,
     * ke kterému byl neoprávněný přístup pokusně proveden.
     *
     * @param playerId identifikátor hráče
     */
    public ForbiddenPlayerAccessException(Long playerId) {
        super("BE - Hráč " + playerId + " nepatří přihlášenému uživateli.", HttpStatus.FORBIDDEN);
    }
}