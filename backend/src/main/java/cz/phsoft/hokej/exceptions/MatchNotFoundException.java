package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že požadovaný zápas nebyl nalezen.
 *
 * <p>
 * Vyhazuje se při práci s ID zápasu, které neexistuje v databázi.
 * </p>
 *
 * Typicky mapováno na HTTP 404 (Not Found).
 */
public class MatchNotFoundException extends BusinessException {

    public MatchNotFoundException(Long matchId) {
        super("BE - Zápas s ID " + matchId + " nenalezen.", HttpStatus.NOT_FOUND);
    }
}
