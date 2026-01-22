package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class MatchNotFoundException extends BusinessException {
    public MatchNotFoundException(Long matchId) {
        super("Zápas s ID " + matchId + " nenalezen.", HttpStatus.NOT_FOUND);
    }
}