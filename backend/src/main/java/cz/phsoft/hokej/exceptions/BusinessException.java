package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Základní doménová výjimka pro business chyby v aplikaci.
 *
 * <p>
 * Slouží jako společný předek pro aplikační výjimky, které nesou
 * informaci o odpovídajícím HTTP status kódu. Tento status
 * je následně použit v globálním handleru výjimek pro tvorbu
 * jednotné chybové odpovědi.
 * </p>
 */
public class BusinessException extends RuntimeException {

    /**
     * HTTP status kód, který má být vrácen klientovi.
     */
    private final HttpStatus status;

    /**
     * Vytvoří novou business výjimku s danou zprávou a HTTP statusem.
     *
     * @param message chybová zpráva určená typicky přímo pro uživatele
     * @param status  HTTP status kód odpovídající dané chybě
     */
    protected BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Vrátí HTTP status kód spojený s touto výjimkou.
     *
     * @return HTTP status kód
     */
    public HttpStatus getStatus() {
        return status;
    }
}
