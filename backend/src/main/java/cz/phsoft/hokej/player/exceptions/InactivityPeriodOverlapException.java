package cz.phsoft.hokej.player.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující překryv období neaktivity pro daného hráče.
 *
 * Vyhazuje se v případě, kdy nové období neaktivity koliduje
 * s již existujícím obdobím téhož hráče.
 *
 * Typicky mapováno na HTTP 409 (Conflict).
 */
public class InactivityPeriodOverlapException extends BusinessException {

    public InactivityPeriodOverlapException() {
        super("BE - Nové období se překrývá s existujícím obdobím neaktivity hráče.", HttpStatus.CONFLICT);
    }

    /**
     * Alternativní konstruktor s vlastní chybovou zprávou.
     */
    public InactivityPeriodOverlapException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
