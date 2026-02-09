package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateSeasonNameException extends BusinessException {
    public DuplicateSeasonNameException(String seasonName) {

        super("Sezóna s názvem: " + seasonName + " již existuje", HttpStatus.CONFLICT);
    }
}
