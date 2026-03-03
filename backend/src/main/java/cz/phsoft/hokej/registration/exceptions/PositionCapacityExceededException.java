package cz.phsoft.hokej.registration.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující překročení kapacity pozice v rámci zápasu.
 *
 * Vyhazuje se v situaci, kdy již není možné přidat dalšího hráče
 * na danou pozici z důvodu naplnění maximální kapacity.
 *
 * Je mapována na HTTP status 409 Conflict.
 */
public class PositionCapacityExceededException extends BusinessException {

    /**
     * Vytváří výjimku s vlastním textem chybové zprávy.
     *
     * @param message text chybové zprávy popisující důvod překročení kapacity
     */
    public PositionCapacityExceededException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}