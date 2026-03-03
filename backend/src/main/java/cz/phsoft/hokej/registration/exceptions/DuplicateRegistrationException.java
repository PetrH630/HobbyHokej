package cz.phsoft.hokej.registration.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že hráč má již aktivní registraci na daný zápas.
 *
 * Vyhazuje se při pokusu o vytvoření nové registrace,
 * pokud již existuje platná registrace pro stejnou kombinaci
 * hráče a zápasu.
 *
 * Standardně je mapována na HTTP status 409 Conflict.
 */
public class DuplicateRegistrationException extends BusinessException {

    /**
     * Vytváří výjimku s předdefinovanou chybovou zprávou.
     *
     * @param matchId  identifikátor zápasu
     * @param playerId identifikátor hráče
     */
    public DuplicateRegistrationException(Long matchId, Long playerId) {
        super("BE - Hráč " + playerId + " již má aktivní registraci na zápas " + matchId + ".", HttpStatus.CONFLICT);
    }

    /**
     * Vytváří výjimku s vlastním textem chybové zprávy.
     *
     * HTTP status je v tomto případě nastaven na 404 Not Found.
     *
     * @param matchId  identifikátor zápasu
     * @param playerId identifikátor hráče
     * @param message  vlastní text chybové zprávy
     */
    public DuplicateRegistrationException(Long matchId, Long playerId, String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}