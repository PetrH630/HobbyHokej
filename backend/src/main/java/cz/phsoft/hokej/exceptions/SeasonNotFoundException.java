package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že požadovaná sezóna nebyla nalezena.
 *
 * <p>
 * Používá se jak v případě neexistující sezóny, tak v situaci,
 * kdy není nastavena žádná aktivní sezóna a aplikace ji vyžaduje.
 * </p>
 *
 * Typicky mapováno na HTTP 409 (Conflict).
 */
public class SeasonNotFoundException extends BusinessException {

    public SeasonNotFoundException() {
        super("BE - Sezóna nebyla nalezena.", HttpStatus.CONFLICT);
    }

    public SeasonNotFoundException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
