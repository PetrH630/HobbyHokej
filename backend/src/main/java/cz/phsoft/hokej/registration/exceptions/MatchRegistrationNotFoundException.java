package cz.phsoft.hokej.registration.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že registrace hráče na daný zápas nebyla nalezena.
 *
 * Vyhazuje se při práci s registrací, která neexistuje pro kombinaci
 * hráč a zápas.
 *
 * Typicky mapováno na HTTP 404 (Not Found).
 */
public class MatchRegistrationNotFoundException extends BusinessException {

    public MatchRegistrationNotFoundException(Long playerId, Long matchId) {
        super("BE - Registrace hráče s ID " + playerId + " na zápas s ID " + matchId + " nenalezena.", HttpStatus.NOT_FOUND);
    }
}
