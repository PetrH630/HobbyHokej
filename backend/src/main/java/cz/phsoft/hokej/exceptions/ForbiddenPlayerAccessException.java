package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenPlayerAccessException extends BusinessException {

    public ForbiddenPlayerAccessException(Long playerId) {
        super("BE - Hráč " + playerId + " nepatří přihlášenému uživateli.", HttpStatus.FORBIDDEN);
    }
}
