package cz.phsoft.hokej.player.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že hráč se stejným jménem a příjmením již existuje.
 *
 * Používá se při vytváření nebo registraci hráče, pokud by došlo
 * k duplicitě podle kombinace jméno a příjmení.
 *
 * Výjimka je mapována na HTTP status 409 Conflict
 * prostřednictvím nadřazené třídy BusinessException.
 */
public class DuplicateNameSurnameException extends BusinessException {

    /**
     * Vytváří výjimku s detailní chybovou zprávou obsahující
     * jméno a příjmení duplicitního hráče.
     *
     * @param name    křestní jméno hráče
     * @param surname příjmení hráče
     */
    public DuplicateNameSurnameException(String name, String surname) {
        super("BE - Hráč se jménem " + name + " " + surname + " již existuje.", HttpStatus.CONFLICT);
    }
}