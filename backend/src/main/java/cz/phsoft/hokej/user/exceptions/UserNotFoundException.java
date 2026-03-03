package cz.phsoft.hokej.user.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že požadovaný uživatel nebyl nalezen.
 *
 * Může být vyhozena při hledání podle e-mailu nebo podle ID uživatele.
 * Reprezentuje situaci, kdy požadovaný zdroj v systému neexistuje.
 *
 * Výjimka je mapována na HTTP status NOT_FOUND.
 */
public class UserNotFoundException extends BusinessException {

    /**
     * Vytvoří výjimku pro případ, kdy uživatel nebyl nalezen podle e-mailu.
     *
     * @param email e-mail hledaného uživatele
     */
    public UserNotFoundException(String email) {
        super("BE - Uživatel s e-mailem " + email + " nenalezen.", HttpStatus.NOT_FOUND);
    }

    /**
     * Vytvoří výjimku pro případ, kdy uživatel nebyl nalezen podle ID.
     *
     * @param id identifikátor hledaného uživatele
     */
    public UserNotFoundException(Long id) {
        super("BE - Uživatel s ID " + id + " nenalezen.", HttpStatus.NOT_FOUND);
    }
}