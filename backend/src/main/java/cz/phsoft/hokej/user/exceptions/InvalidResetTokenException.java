package cz.phsoft.hokej.user.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatný nebo expirovaný resetovací token.
 *
 * Používá se při procesu resetu hesla, pokud je odkaz neplatný,
 * již použitý nebo expirovaný.
 */
public class InvalidResetTokenException extends BusinessException {

    /**
     * Vytvoří výjimku s předdefinovanou chybovou zprávou
     * a HTTP statusem NOT_FOUND.
     */
    public InvalidResetTokenException() {
        super("BE - Resetovací odkaz je neplatný nebo expirovaný.", HttpStatus.NOT_FOUND);
    }

    /**
     * Alternativní konstruktor s vlastním textem zprávy.
     *
     * V tomto případě je použit HTTP status GONE,
     * který signalizuje, že zdroj byl dříve dostupný,
     * ale již není platný.
     *
     * @param message vlastní chybová zpráva
     */
    public InvalidResetTokenException(String message) {
        super(message, HttpStatus.GONE);
    }
}