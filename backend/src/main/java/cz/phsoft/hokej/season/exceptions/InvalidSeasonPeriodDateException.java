package cz.phsoft.hokej.season.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatné datumové rozmezí sezóny.
 *
 * Používá se v servisní vrstvě při validaci vstupních dat,
 * pokud datum začátku sezóny není před datem konce nebo
 * pokud dochází k jinému porušení logických pravidel
 * definujících časové období sezóny.
 *
 * Výjimka reprezentuje porušení doménového pravidla
 * a je typicky mapována na HTTP status BAD_REQUEST.
 */
public class InvalidSeasonPeriodDateException extends BusinessException {

    /**
     * Vytvoří výjimku s konkrétní chybovou zprávou
     * popisující porušení pravidla časového rozmezí.
     *
     * @param message detailní popis chyby
     */
    public InvalidSeasonPeriodDateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}