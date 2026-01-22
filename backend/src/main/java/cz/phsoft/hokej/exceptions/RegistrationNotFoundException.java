package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class RegistrationNotFoundException extends BusinessException {
    public RegistrationNotFoundException(Long matchId, Long playerId) {
        super("BE - Hráč " + playerId + " nemá registraci na zápas " + matchId,
                HttpStatus.NOT_FOUND);
    }
}