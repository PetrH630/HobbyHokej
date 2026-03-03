package cz.phsoft.hokej.season.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující nepovolený stavový přechod sezóny.
 *
 * Používá se v situacích, kdy operace nad sezónou není povolena
 * vzhledem k jejímu aktuálnímu stavu. Typickým příkladem je pokus
 * o deaktivaci poslední aktivní sezóny nebo jiná neplatná změna
 * příznaku aktivace.
 *
 * Výjimka reprezentuje porušení doménových pravidel týkajících se
 * životního cyklu sezóny a je typicky mapována na HTTP status CONFLICT.
 */
public class InvalidSeasonStateException extends BusinessException {

    /**
     * Vytvoří výjimku s konkrétní chybovou zprávou
     * popisující neplatný stavový přechod.
     *
     * @param message detailní popis porušení pravidla
     */
    public InvalidSeasonStateException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}