package cz.phsoft.hokej.user.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatnou nebo nepovolenou změnu
 * aktivačního stavu uživatelského účtu.
 *
 * Typicky se vyhazuje v situacích, kdy:
 * je uživatel již aktivní a pokusí se o další aktivaci,
 * je uživatel neaktivní a operace neodpovídá jeho stavu,
 * nebo dojde k porušení pravidel životního cyklu uživatele.
 *
 * Výjimka je mapována na HTTP status CONFLICT.
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