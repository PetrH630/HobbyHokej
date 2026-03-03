package cz.phsoft.hokej.player.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že požadovaný hráč nebyl nalezen.
 *
 * Může být vyhozena jak při hledání podle ID, tak při hledání
 * podle e-mailu.
 *
 * Výjimka je mapována na HTTP status 404 Not Found.
 */
public class PlayerNotFoundException extends BusinessException {

    /**
     * Vytváří výjimku pro situaci, kdy hráč s daným ID nebyl nalezen.
     *
     * @param playerId identifikátor hráče
     */
    public PlayerNotFoundException(Long playerId) {
        super("BE - Hráč s ID " + playerId + " nenalezen.", HttpStatus.NOT_FOUND);
    }

    /**
     * Vytváří výjimku pro situaci, kdy hráč s daným e-mailem nebyl nalezen.
     *
     * @param email e-mail hráče
     */
    public PlayerNotFoundException(String email) {
        super("BE - Hráč s e-mailem " + email + " nenalezen.", HttpStatus.NOT_FOUND);
    }

    /**
     * Vytváří výjimku s vlastní chybovou zprávou.
     *
     * @param message text chybové zprávy
     * @param email   e-mail hráče použitý při vyhledávání
     */
    public PlayerNotFoundException(String message, String email) {
        super(message, HttpStatus.NOT_FOUND);
    }
}