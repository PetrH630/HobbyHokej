package cz.phsoft.hokej.player.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatný nebo nepovolený stav hráče.
 *
 * Používá se při změně nebo vyhodnocování stavu hráče, pokud
 * požadovaná operace není v daném stavu povolena.
 *
 * Výjimka je mapována na HTTP status 400 Bad Request.
 */
public class InvalidPlayerStatusException extends BusinessException {

    /**
     * Vytváří výjimku s vlastní chybovou zprávou.
     *
     * @param message text chybové zprávy
     */
    public InvalidPlayerStatusException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}