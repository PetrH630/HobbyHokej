package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že požadovaná sezóna nebyla nalezena.
 *
 * <p>
 * Používá se jak při hledání sezóny podle ID, tak v situaci,
 * kdy není nastavena žádná aktivní sezóna.
 * </p>
 *
 * Typicky mapováno na HTTP 404 (Not Found).
 */
public class SeasonNotFoundException extends BusinessException {

    /**
     * Sezóna s konkrétním ID nebyla nalezena.
     *
     * @param id ID sezóny
     */
    public SeasonNotFoundException(Long id) {
        super("BE - Sezóna s ID " + id + " nebyla nalezena.", HttpStatus.NOT_FOUND);
    }

    /**
     * Obecnější varianta s custom zprávou.
     *
     * Typicky použitá pro situaci typu „není nastavena žádná aktivní sezóna“.
     *
     * @param message chybová zpráva
     */
    public SeasonNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
