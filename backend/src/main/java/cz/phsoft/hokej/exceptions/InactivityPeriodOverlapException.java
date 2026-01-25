package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující překryv období neaktivity pro daného hráče.
 *
 * <p>
 * Vyhazuje se v případě, kdy nové období neaktivity koliduje
 * s již existujícím obdobím téhož hráče.
 * </p>
 *
 * Typicky mapováno na HTTP 409 (Conflict).
 */
public class InactivityPeriodOverlapException extends BusinessException {

    public InactivityPeriodOverlapException() {
        super("BE - Nové období se překrývá s existujícím obdobím neaktivity hráče.", HttpStatus.CONFLICT);
    }

    public InactivityPeriodOverlapException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
