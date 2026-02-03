package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatnou změnu stavu zápasu.
 *
 * Vyhazuje se při pokusu o přechod zápasu do stavu, který není
 * povolen vzhledem k aktuálnímu stavu nebo business pravidlům.
 *
 * Typicky mapováno na HTTP 409 (Conflict).
 */
public class InvalidMatchStatusException extends BusinessException {

    public InvalidMatchStatusException(Long id, String message) {
        super("BE - Zápas s ID " + id + " - " + message, HttpStatus.CONFLICT);
    }
}
