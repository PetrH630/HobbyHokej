package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String email) {
        super("Uživatel s emailem " + email + " nenalezen.", HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(Long id) {
        super("Uživatel s ID " + id + " nenalezen.", HttpStatus.NOT_FOUND);
    }
}
