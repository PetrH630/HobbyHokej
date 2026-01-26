package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující neplatný pokus o změnu přiřazení hráče k uživateli.
 * <p>
 * Vyhazuje se v případě, kdy se aplikace pokusí přiřadit hráče
 * ke stejnému uživateli, ke kterému již patří.
 * </p>
 *
 * Tato výjimka:
 * <ul>
 *     <li>rozšiřuje {@link BusinessException},</li>
 *     <li>mapuje se na HTTP status {@link HttpStatus#CONFLICT} (409),</li>
 *     <li>označuje logický konflikt ve stavu dat.</li>
 * </ul>
 *
 * Používá se v service vrstvě jako ochrana proti zbytečné
 * nebo neplatné změně vazby hráč → uživatel.
 */
public class InvalidChangePlayerUserException extends BusinessException {

    /**
     * Vytvoří výjimku s předdefinovanou chybovou zprávou.
     */
    public InvalidChangePlayerUserException() {
        super("Hráč už je přiřazen tomuto uživateli", HttpStatus.CONFLICT);
    }
}
