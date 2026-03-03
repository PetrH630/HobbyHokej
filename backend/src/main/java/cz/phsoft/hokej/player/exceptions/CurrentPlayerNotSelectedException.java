package cz.phsoft.hokej.player.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že není zvolen aktuální hráč.
 *
 * Vyhazuje se v situaci, kdy je volán endpoint nebo servisní metoda,
 * která vyžaduje nastaveného aktuálního hráče, ale pro daného
 * uživatele není žádný hráč vybrán.
 *
 * Výjimka je mapována na HTTP status 400 Bad Request
 * prostřednictvím nadřazené třídy BusinessException.
 */
public class CurrentPlayerNotSelectedException extends BusinessException {

    /**
     * Vytváří výjimku s předdefinovanou chybovou zprávou
     * a HTTP statusem BAD_REQUEST.
     */
    public CurrentPlayerNotSelectedException() {
        super("BE - Není zvolen aktuální hráč.", HttpStatus.BAD_REQUEST);
    }
}