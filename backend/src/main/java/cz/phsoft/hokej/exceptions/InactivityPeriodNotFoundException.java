package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class InactivityPeriodNotFoundException extends BusinessException {
    public InactivityPeriodNotFoundException(Long id) {
        super("BE - Období neaktivity s ID " + id + " neexistuje.", HttpStatus.NOT_FOUND);
    }
}