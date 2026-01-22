package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String email) {
        super("BE - Uživatel s emailem " + email + " nenalezen.", HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(Long id) {
        super("BE - Uživatel s ID " + id + " nenalezen.", HttpStatus.NOT_FOUND);
    }
}
