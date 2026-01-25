package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující překryv období mezi sezónami.
 *
 * <p>
 * Vyhazuje se při vytváření nebo úpravě sezóny, pokud její období
 * zasahuje do období jiné existující sezóny.
 * </p>
 *
 * Typicky mapováno na HTTP 409 (Conflict).
 */
public class SeasonPeriodOverlapException extends BusinessException {

    public SeasonPeriodOverlapException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
