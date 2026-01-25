package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že požadovaný hráč nebyl nalezen.
 *
 * <p>
 * Může být vyhozena jak při hledání podle ID, tak při hledání podle e-mailu.
 * </p>
 *
 * Typicky mapováno na HTTP 404 (Not Found).
 */
public class PlayerNotFoundException extends BusinessException {

    public PlayerNotFoundException(Long playerId) {
        super("BE - Hráč s ID " + playerId + " nenalezen.", HttpStatus.NOT_FOUND);
    }

    public PlayerNotFoundException(String email) {
        super("BE - Hráč s e-mailem " + email + " nenalezen.", HttpStatus.NOT_FOUND);
    }
}
