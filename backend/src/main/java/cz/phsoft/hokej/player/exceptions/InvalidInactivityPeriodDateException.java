package cz.phsoft.hokej.player.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatné datumové rozmezí období neaktivity.
 *
 * Typicky se používá, pokud datum "od" je po datu "do"
 * nebo jinak nesplňuje logická validační pravidla.
 *
 * Typicky mapováno na HTTP 400 (Bad Request).
 */
public class InvalidInactivityPeriodDateException extends BusinessException {

    public InvalidInactivityPeriodDateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
