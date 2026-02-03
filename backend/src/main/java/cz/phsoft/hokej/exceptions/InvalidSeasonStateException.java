package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující nepovolený stavový přechod sezóny.
 *
 * Používá se v situacích, kdy operace nad sezónou není povolena
 * vzhledem k jejímu aktuálnímu stavu, například při deaktivaci
 * poslední aktivní sezóny nebo při neplatné změně aktivace.
 *
 * Typicky mapováno na HTTP 409 (Conflict).
 */
public class InvalidSeasonStateException extends BusinessException {

    public InvalidSeasonStateException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
