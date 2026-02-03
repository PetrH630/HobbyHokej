package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že požadovaný uživatel nebyl nalezen.
 *
 * Může být vyhozena při hledání podle e-mailu nebo podle ID uživatele.
 *
 * Typicky mapováno na HTTP 404 (Not Found).
 */
public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String email) {
        super("BE - Uživatel s e-mailem " + email + " nenalezen.", HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(Long id) {
        super("BE - Uživatel s ID " + id + " nenalezen.", HttpStatus.NOT_FOUND);
    }
}
