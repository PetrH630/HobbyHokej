package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující nepovolený stavový přechod sezóny.
 *
 * <p>
 * Používá se v situacích, kdy operace nad sezónou není povolena
 * vzhledem k jejímu aktuálnímu stavu (např. deaktivace poslední
 * aktivní sezóny nebo neplatná změna aktivace).
 * </p>
 *
 * Typicky mapováno na HTTP 409 (Conflict).
 */
public class InvalidSeasonStateException extends BusinessException {

    public InvalidSeasonStateException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
