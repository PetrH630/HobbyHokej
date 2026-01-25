package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že hráč se stejným jménem a příjmením již existuje.
 *
 * <p>
 * Používá se při vytváření nebo registraci hráče, pokud by došlo
 * k duplicitě podle kombinace jméno + příjmení.
 * </p>
 *
 * Typicky mapováno na HTTP 409 (Conflict).
 */
public class DuplicateNameSurnameException extends BusinessException {

    public DuplicateNameSurnameException(String name, String surname) {
        super(
                "BE - Hráč se jménem " + name + " " + surname + " již existuje.",
                HttpStatus.CONFLICT
        );
    }
}
