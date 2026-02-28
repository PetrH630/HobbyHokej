package cz.phsoft.hokej.player.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že není zvolen aktuální hráč.
 *
 * Vyhazuje se v situaci, kdy klient volá endpoint, který vyžaduje
 * nastaveného "current player", ale žádný hráč není pro daného
 * uživatele vybrán.
 *
 * Typicky mapováno na HTTP 400 (Bad Request).
 */
public class CurrentPlayerNotSelectedException extends BusinessException {

    public CurrentPlayerNotSelectedException() {
        super("BE - Není zvolen aktuální hráč.", HttpStatus.BAD_REQUEST);
    }
}
