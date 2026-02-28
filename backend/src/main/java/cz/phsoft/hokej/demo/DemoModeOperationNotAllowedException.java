package cz.phsoft.hokej.demo;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že operace není v DEMO režimu povolena.
 *
 * Používá se pro zakázání destruktivních operací (např. změna hesla,
 * mazání dat apod.) v demo instanci aplikace. Umožňuje vrátit
 * uživatelsky přívětivou hlášku na frontend.
 */

public class DemoModeOperationNotAllowedException extends BusinessException {

    public DemoModeOperationNotAllowedException(String message) {

        super(message, HttpStatus.METHOD_NOT_ALLOWED);
    }
}
