package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatnou nebo nepovolenou změnu
 * aktivačního stavu uživatelského účtu.
 * <p>
 * Typicky se vyhazuje v situacích, kdy:
 * <ul>
 *     <li>je uživatel již aktivní a pokusíme se ho znovu aktivovat,</li>
 *     <li>je uživatel neaktivní a operace neodpovídá jeho aktuálnímu stavu,</li>
 *     <li>dojde k porušení pravidel životního cyklu uživatele.</li>
 * </ul>
 *
 * Tato výjimka:
 * <ul>
 *     <li>rozšiřuje {@link BusinessException},</li>
 *     <li>mapuje se na HTTP status {@link HttpStatus#CONFLICT} (409),</li>
 *     <li>signalizuje logický konflikt stavu zdroje.</li>
 * </ul>
 *
 * Používá se v service vrstvě, nikoli přímo v controllerech.
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
