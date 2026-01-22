package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateRegistrationException extends BusinessException {
    public DuplicateRegistrationException(Long matchId, Long playerId) {
        super("BE - Hráč " + playerId + " již má aktivní registraci na zápas " + matchId,
                HttpStatus.CONFLICT); // 409
    }
}