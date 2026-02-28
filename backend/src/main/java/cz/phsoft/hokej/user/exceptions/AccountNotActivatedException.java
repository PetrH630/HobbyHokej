package cz.phsoft.hokej.user.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že uživatelský účet není aktivován.
 *
 * Vyhazuje se v případě, kdy se uživatel pokouší přihlásit
 * nebo používat funkce vyžadující aktivovaný účet, aniž by
 * dokončil aktivaci přes odkaz v e-mailu.
 *
 * Typicky mapováno na HTTP 403 (Forbidden).
 */
public class AccountNotActivatedException extends BusinessException {

    /**
     * Vytváří výjimku s předdefinovanou chybovou zprávou.
     *
     * Zpráva popisuje, že uživatelský účet není aktivován
     * a je určena pro zobrazení v chybové odpovědi API.
     */
    public AccountNotActivatedException() {
        super("BE - Nejprve musíte aktivovat účet pomocí odkazu v e-mailu, nebo kontaktujte administrátora.", HttpStatus.FORBIDDEN);
    }
}
