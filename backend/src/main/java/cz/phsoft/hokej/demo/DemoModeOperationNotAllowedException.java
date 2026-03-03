package cz.phsoft.hokej.demo;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že operace není v demo režimu povolena.
 *
 * Používá se pro zakázání destruktivních operací v demo instanci aplikace,
 * například změny hesla nebo mazání dat. Umožňuje vrátit uživatelsky
 * přívětivou chybovou zprávu na frontend ve standardním formátu BusinessException.
 */
public class DemoModeOperationNotAllowedException extends BusinessException {

    /**
     * Vytváří výjimku pro nepovolenou operaci v demo režimu.
     *
     * @param message chybová zpráva určená pro zobrazení uživateli
     */
    public DemoModeOperationNotAllowedException(String message) {
        super(message, HttpStatus.METHOD_NOT_ALLOWED);
    }
}