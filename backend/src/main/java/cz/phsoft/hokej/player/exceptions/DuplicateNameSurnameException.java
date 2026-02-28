package cz.phsoft.hokej.player.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že hráč se stejným jménem a příjmením již existuje.
 *
 * Používá se při vytváření nebo registraci hráče, pokud by došlo
 * k duplicitě podle kombinace jméno a příjmení.
 *
 * Typicky mapováno na HTTP 409 (Conflict).
 */
public class DuplicateNameSurnameException extends BusinessException {

    public DuplicateNameSurnameException(String name, String surname) {
        super("BE - Hráč se jménem " + name + " " + surname + " již existuje.", HttpStatus.CONFLICT);
    }
}
