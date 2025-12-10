package cz.phsoft.hokej.exceptions;

public class RegistrationNotFoundException extends RuntimeException {
    public RegistrationNotFoundException(Long matchId, Long playerId) {
        super("Hráč " + playerId + " nemá registraci na zápas " + matchId);
    }
}
