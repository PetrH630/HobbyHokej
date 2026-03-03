package cz.phsoft.hokej.match.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že požadovaný zápas nebyl nalezen.
 *
 * Vyhazuje se při práci s identifikátorem zápasu,
 * který neexistuje v databázi.
 *
 * Výjimka je mapována na HTTP status 404 Not Found.
 */
public class MatchNotFoundException extends BusinessException {

    /**
     * Vytváří výjimku pro nenalezený zápas.
     *
     * @param matchId identifikátor nenalezeného zápasu
     */
    public MatchNotFoundException(Long matchId) {
        super("BE - Zápas s ID " + matchId + " nenalezen.", HttpStatus.NOT_FOUND);
    }
}