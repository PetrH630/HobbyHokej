package cz.phsoft.hokej.season.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující překryv období mezi sezónami.
 *
 * Vyhazuje se při vytváření nebo úpravě sezóny, pokud její období
 * zasahuje do období jiné existující sezóny.
 *
 * Typicky mapováno na HTTP 409 (Conflict).
 */
public class SeasonPeriodOverlapException extends BusinessException {

    public SeasonPeriodOverlapException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
