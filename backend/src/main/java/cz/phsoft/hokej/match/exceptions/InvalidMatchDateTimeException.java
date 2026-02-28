package cz.phsoft.hokej.match.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatné datum nebo čas zápasu.
 *
 * Používá se například v situacích, kdy je zápas plánován
 * v minulosti nebo kdy datum a čas nesplňují validační pravidla
 * aplikace.
 *
 * Typicky mapováno na HTTP 400 (Bad Request).
 */
public class InvalidMatchDateTimeException extends BusinessException {

    public InvalidMatchDateTimeException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
