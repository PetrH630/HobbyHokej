package cz.phsoft.hokej.match.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že požadovaný zápas nebyl nalezen.
 *
 * Vyhazuje se při práci s ID zápasu, které neexistuje v databázi.
 *
 * Typicky mapováno na HTTP 404 (Not Found).
 */
public class MatchNotFoundException extends BusinessException {

    public MatchNotFoundException(Long matchId) {
        super("BE - Zápas s ID " + matchId + " nenalezen.", HttpStatus.NOT_FOUND);
    }
}
