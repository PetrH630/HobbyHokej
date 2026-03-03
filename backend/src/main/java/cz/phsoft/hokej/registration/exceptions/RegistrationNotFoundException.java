package cz.phsoft.hokej.registration.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že hráč nemá registraci na daný zápas.
 *
 * Vyhazuje se při pokusu o práci s registrací, která
 * pro kombinaci hráče a zápasu neexistuje,
 * například při odhlášení neexistující registrace.
 *
 * Je mapována na HTTP status 404 Not Found.
 */
public class RegistrationNotFoundException extends BusinessException {

    /**
     * Vytváří výjimku pro případ neexistující registrace hráče na zápas.
     *
     * @param matchId  identifikátor zápasu
     * @param playerId identifikátor hráče
     */
    public RegistrationNotFoundException(Long matchId, Long playerId) {
        super("BE - Hráč " + playerId + " nemá registraci na zápas " + matchId + ".", HttpStatus.NOT_FOUND);
    }
}