package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class PlayerNotFoundException extends BusinessException {
    public PlayerNotFoundException(Long playerId) {
        super("Hráč s ID " + playerId + " nenalezen.", HttpStatus.NOT_FOUND);
    }

    public PlayerNotFoundException(String email) {
        super("Hráč s emailem " + email + " nenalezen.", HttpStatus.NOT_FOUND);
    }
}
