package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatný nebo expirovaný resetovací token.
 *
 * Používá se při procesu resetu hesla, pokud je odkaz neplatný,
 * již použitý nebo expirovaný.
 */
public class InvalidResetTokenException extends BusinessException {

    public InvalidResetTokenException() {
        super("BE - Resetovací odkaz je neplatný nebo expirovaný.", HttpStatus.NOT_FOUND);
    }

    /**
     * Alternativní konstruktor s vlastním textem zprávy.
     *
     * V tomto případě je použit HTTP status 410 (Gone).
     */
    public InvalidResetTokenException(String message) {
        super(message, HttpStatus.GONE);
    }
}
