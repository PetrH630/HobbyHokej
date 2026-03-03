package cz.phsoft.hokej.match.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatné datum nebo čas zápasu.
 *
 * Používá se při porušení validačních pravidel plánování zápasu,
 * například pokud je datum v minulosti nebo neodpovídá
 * doménovým omezením aplikace.
 *
 * Výjimka je mapována na HTTP status 400 Bad Request.
 */
public class InvalidMatchDateTimeException extends BusinessException {

    /**
     * Vytváří výjimku pro neplatné datum nebo čas zápasu.
     *
     * @param message detailní popis porušení validačního pravidla
     */
    public InvalidMatchDateTimeException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}