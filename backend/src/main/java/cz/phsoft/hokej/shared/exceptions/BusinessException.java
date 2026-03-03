package cz.phsoft.hokej.shared.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Základní doménová výjimka pro business chyby aplikace.
 *
 * Slouží jako společný předek pro aplikační výjimky,
 * které nesou informaci o odpovídajícím HTTP status kódu.
 * Tento status je následně použit v globálním handleru
 * výjimek pro vytvoření jednotné chybové odpovědi.
 */
public class BusinessException extends RuntimeException {

    /**
     * HTTP status kód, který má být vrácen klientovi.
     */
    private final HttpStatus status;

    /**
     * Vytvoří novou business výjimku.
     *
     * @param message chybová zpráva určená pro klienta
     * @param status HTTP status kód odpovídající dané chybě
     */
    protected BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Vrací HTTP status kód spojený s touto výjimkou.
     *
     * @return HTTP status kód
     */
    public HttpStatus getStatus() {
        return status;
    }
}