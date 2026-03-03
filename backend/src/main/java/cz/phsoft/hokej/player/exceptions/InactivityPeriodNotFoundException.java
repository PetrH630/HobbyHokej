package cz.phsoft.hokej.player.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že požadované období neaktivity nebylo nalezeno.
 *
 * Vyhazuje se při práci s ID období neaktivity, které neexistuje
 * v databázi.
 *
 * Výjimka je mapována na HTTP status 404 Not Found.
 */
public class InactivityPeriodNotFoundException extends BusinessException {

    /**
     * Vytváří výjimku s chybovou zprávou obsahující ID
     * nenalezeného období neaktivity.
     *
     * @param id identifikátor období neaktivity
     */
    public InactivityPeriodNotFoundException(Long id) {
        super("BE - Období neaktivity s ID " + id + " neexistuje.", HttpStatus.NOT_FOUND);
    }
}