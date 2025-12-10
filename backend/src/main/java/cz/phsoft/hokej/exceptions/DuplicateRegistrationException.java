package cz.phsoft.hokej.exceptions;

public class DuplicateRegistrationException extends RuntimeException {
    public DuplicateRegistrationException(Long matchId, Long playerId) {
        super("Hráč " + playerId + " již má aktivní registraci na zápas " + matchId);
    }
}