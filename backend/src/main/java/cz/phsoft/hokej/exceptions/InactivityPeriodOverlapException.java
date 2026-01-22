package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class InactivityPeriodOverlapException extends BusinessException {
    public InactivityPeriodOverlapException() {
        super("BE - Nové období se překrývá s existujícím obdobím neaktivity hráče.", HttpStatus.CONFLICT);
    }
    public InactivityPeriodOverlapException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}