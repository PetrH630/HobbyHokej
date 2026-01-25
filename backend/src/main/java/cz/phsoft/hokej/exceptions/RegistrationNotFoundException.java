package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že hráč nemá registraci na daný zápas.
 *
 * <p>
 * Vyhazuje se při pokusu o práci s registrací, která pro kombinaci
 * hráč + zápas neexistuje (např. při odhlášení neexistující registrace).
 * </p>
 *
 * Typicky mapováno na HTTP 404 (Not Found).
 */
public class RegistrationNotFoundException extends BusinessException {

    public RegistrationNotFoundException(Long matchId, Long playerId) {
        super(
                "BE - Hráč " + playerId + " nemá registraci na zápas " + matchId + ".",
                HttpStatus.NOT_FOUND
        );
    }
}
