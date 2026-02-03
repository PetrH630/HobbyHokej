package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že požadované období neaktivity nebylo nalezeno.
 *
 * Vyhazuje se při práci s ID období neaktivity, které neexistuje
 * v databázi.
 *
 * Typicky mapováno na HTTP 404 (Not Found).
 */
public class InactivityPeriodNotFoundException extends BusinessException {

    public InactivityPeriodNotFoundException(Long id) {
        super("BE - Období neaktivity s ID " + id + " neexistuje.", HttpStatus.NOT_FOUND);
    }
}
