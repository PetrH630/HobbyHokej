package cz.phsoft.hokej.player.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující překryv období neaktivity pro daného hráče.
 *
 * Vyhazuje se v případě, kdy nové období neaktivity koliduje
 * s již existujícím obdobím téhož hráče.
 *
 * Výjimka je mapována na HTTP status 409 Conflict.
 */
public class InactivityPeriodOverlapException extends BusinessException {

    /**
     * Vytváří výjimku s výchozí chybovou zprávou.
     */
    public InactivityPeriodOverlapException() {
        super("BE - Nové období se překrývá s existujícím obdobím neaktivity hráče.", HttpStatus.CONFLICT);
    }

    /**
     * Vytváří výjimku s vlastní chybovou zprávou.
     *
     * @param message text chybové zprávy
     */
    public InactivityPeriodOverlapException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}