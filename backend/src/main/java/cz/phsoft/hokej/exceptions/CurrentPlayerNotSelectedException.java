package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že není zvolen aktuální hráč.
 *
 * <p>
 * Vyhazuje se v situaci, kdy klient volá endpoint, který vyžaduje
 * nastaveného "current player", ale žádný hráč není pro daného
 * uživatele vybrán.
 * </p>
 *
 * Typicky mapováno na HTTP 400 (Bad Request).
 */
public class CurrentPlayerNotSelectedException extends BusinessException {

    public CurrentPlayerNotSelectedException() {
        super("BE - Není zvolen aktuální hráč.", HttpStatus.BAD_REQUEST);
    }
}
