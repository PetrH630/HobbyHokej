package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatnou nebo nepovolenou změnu
 * aktivačního stavu uživatelského účtu.
 *
 * Typicky se vyhazuje v situacích, kdy:
 * - je uživatel již aktivní a pokusí se o další aktivaci,
 * - je uživatel neaktivní a operace neodpovídá jeho stavu,
 * - dojde k porušení pravidel životního cyklu uživatele.
 *
 * Typicky mapováno na HTTP 409 (Conflict).
 */
public class InvalidUserActivationException extends BusinessException {

    /**
     * Vytvoří výjimku s popisnou chybovou zprávou.
     *
     * @param message detailní popis důvodu neplatné aktivace
     */
    public InvalidUserActivationException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
