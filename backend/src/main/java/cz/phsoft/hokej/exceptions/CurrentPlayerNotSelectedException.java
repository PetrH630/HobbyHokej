package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class CurrentPlayerNotSelectedException extends BusinessException {

    public CurrentPlayerNotSelectedException() {
        super("BE - Není zvolen aktuální hráč.", HttpStatus.BAD_REQUEST);
        // alternativně by šel i HttpStatus.CONFLICT, ale BAD_REQUEST dává smysl:
        // klient volá endpoint, který vyžaduje current player, ale žádného nenastavil
    }
}
