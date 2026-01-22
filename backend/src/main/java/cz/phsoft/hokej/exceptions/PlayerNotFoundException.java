package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class PlayerNotFoundException extends BusinessException {
    public PlayerNotFoundException(Long playerId) {
        super("BE - Hráč s ID " + playerId + " nenalezen.", HttpStatus.NOT_FOUND);
    }

    public PlayerNotFoundException(String email) {
        super("BE - Hráč s emailem " + email + " nenalezen.", HttpStatus.NOT_FOUND);
    }
}
