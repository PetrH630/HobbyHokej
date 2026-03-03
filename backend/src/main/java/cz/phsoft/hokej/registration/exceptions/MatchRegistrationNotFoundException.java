package cz.phsoft.hokej.registration.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že registrace hráče na daný zápas nebyla nalezena.
 *
 * Vyhazuje se při práci s registrací, která neexistuje
 * pro danou kombinaci hráče a zápasu.
 *
 * Je mapována na HTTP status 404 Not Found.
 */
public class MatchRegistrationNotFoundException extends BusinessException {

    /**
     * Vytváří výjimku pro nenalezenou registraci hráče na zápas.
     *
     * @param playerId identifikátor hráče
     * @param matchId  identifikátor zápasu
     */
    public MatchRegistrationNotFoundException(Long playerId, Long matchId) {
        super("BE - Registrace hráče s ID " + playerId + " na zápas s ID " + matchId + " nenalezena.", HttpStatus.NOT_FOUND);
    }
}