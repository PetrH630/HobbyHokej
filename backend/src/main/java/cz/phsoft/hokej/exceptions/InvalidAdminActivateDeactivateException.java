package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatný pokus administrátora
 * o aktivaci nebo deaktivaci entity.
 *
 * Používá se v servisní vrstvě v situacích,
 * kdy požadovaná operace není z hlediska
 * aplikačních pravidel povolena.
 *
 * Výjimka je zpracována globálním exception handlerem
 * a vrací HTTP status METHOD_NOT_ALLOWED.
 *
 * Dědí z BusinessException, která reprezentuje
 * porušení doménových pravidel aplikace.
 */
public class InvalidAdminActivateDeactivateException extends BusinessException {

    /**
     * Vytvoří výjimku s detailní chybovou zprávou.
     *
     * @param message text popisující důvod nepovolené operace
     */
    public InvalidAdminActivateDeactivateException(String message) {
        super(message, HttpStatus.METHOD_NOT_ALLOWED);
    }
}
