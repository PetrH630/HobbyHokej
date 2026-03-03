package cz.phsoft.hokej.player.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatné datumové rozmezí období neaktivity.
 *
 * Používá se v případě, kdy datum začátku je po datu konce
 * nebo jinak nesplňuje logická validační pravidla.
 *
 * Výjimka je mapována na HTTP status 400 Bad Request.
 */
public class InvalidInactivityPeriodDateException extends BusinessException {

    /**
     * Vytváří výjimku s vlastní chybovou zprávou.
     *
     * @param message text chybové zprávy
     */
    public InvalidInactivityPeriodDateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}