package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatný nebo nepovolený stav hráče.
 *
 * <p>
 * Používá se při změně nebo vyhodnocování stavu hráče, pokud
 * požadovaná operace není v daném stavu povolena.
 * </p>
 *
 * Typicky mapováno na HTTP 400 (Bad Request).
 */
public class InvalidPlayerStatusException extends BusinessException {

    public InvalidPlayerStatusException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
