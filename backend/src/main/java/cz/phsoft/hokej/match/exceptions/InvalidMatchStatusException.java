package cz.phsoft.hokej.match.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatnou změnu stavu zápasu.
 *
 * Vyhazuje se při pokusu o přechod zápasu do stavu,
 * který není povolen vzhledem k aktuálnímu stavu
 * nebo definovaným business pravidlům.
 *
 * Výjimka je mapována na HTTP status 409 Conflict.
 */
public class InvalidMatchStatusException extends BusinessException {

    /**
     * Vytváří výjimku pro neplatnou změnu stavu zápasu.
     *
     * @param id identifikátor zápasu
     * @param message detailní popis porušení pravidla přechodu stavu
     */
    public InvalidMatchStatusException(Long id, String message) {
        super("BE - Zápas s ID " + id + " - " + message, HttpStatus.CONFLICT);
    }
}