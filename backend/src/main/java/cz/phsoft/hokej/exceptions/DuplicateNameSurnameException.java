package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateNameSurnameException extends BusinessException {
    public DuplicateNameSurnameException(String name, String surname) {
        super(
                "BE - Hráč se jménem " + name + " " + surname + " již existuje.",
                HttpStatus.CONFLICT
        );
    }
}
