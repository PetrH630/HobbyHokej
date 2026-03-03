package cz.phsoft.hokej.season.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující překryv období mezi sezónami.
 *
 * Vyhazuje se při vytváření nebo úpravě sezóny, pokud její období
 * zasahuje do období jiné existující sezóny. Reprezentuje porušení
 * doménového pravidla, podle kterého se jednotlivé sezóny
 * nesmí časově překrývat.
 *
 * Výjimka je typicky mapována na HTTP status CONFLICT.
 */
public class SeasonPeriodOverlapException extends BusinessException {

    /**
     * Vytvoří výjimku s detailní zprávou popisující překryv období.
     *
     * @param message detailní popis kolize období
     */
    public SeasonPeriodOverlapException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}