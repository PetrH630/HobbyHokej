package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že hráč má již aktivní registraci na daný zápas.
 *
 * <p>
 * Vyhazuje se při pokusu o vytvoření nové registrace na zápas,
 * pokud už existuje platná registrace pro stejného hráče a zápas.
 * </p>
 *
 * Typicky mapováno na HTTP 409 (Conflict).
 */
public class DuplicateRegistrationException extends BusinessException {

    public DuplicateRegistrationException(Long matchId, Long playerId) {
        super(
                "BE - Hráč " + playerId + " již má aktivní registraci na zápas " + matchId + ".",
                HttpStatus.CONFLICT
        );
    }
    public DuplicateRegistrationException(Long matchId, Long playerId, String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
