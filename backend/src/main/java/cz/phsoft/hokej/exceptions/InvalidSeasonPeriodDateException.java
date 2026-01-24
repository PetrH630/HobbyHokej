package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidSeasonPeriodDateException extends BusinessException {
    public InvalidSeasonPeriodDateException(String message){
        super(message, HttpStatus.BAD_REQUEST);
    }
}
