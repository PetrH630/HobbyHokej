package cz.phsoft.hokej.season.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatné datumové rozmezí sezóny.
 *
 * Typicky se používá v případě, kdy datum začátku sezóny
 * není před datem konce nebo porušuje jiná logická pravidla.
 *
 * Typicky mapováno na HTTP 400 (Bad Request).
 */
public class InvalidSeasonPeriodDateException extends BusinessException {

    public InvalidSeasonPeriodDateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
