package cz.phsoft.hokej.exceptions;

/**
 * Výjimka signalizující neplatný stav sezóny.
 *
 * <p>
 * Používá se v situacích, kdy operace nad sezónou není povolena
 * vzhledem k jejímu aktuálnímu stavu (např. nelze aktivovat již
 * aktivní sezónu, nelze upravovat archivovanou sezónu apod.).
 * </p>
 *
 * Typicky mapováno na HTTP 400 nebo 409 podle konkrétního handleru.
 */
public class InvalidSeasonStateException extends RuntimeException {

    public InvalidSeasonStateException(String message) {
        super(message);
    }
}
