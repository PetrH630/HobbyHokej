package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class SeasonPeriodOverlapException extends BusinessException {

       public SeasonPeriodOverlapException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
