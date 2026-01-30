package cz.phsoft.hokej.exceptions;

import cz.phsoft.hokej.data.entities.ForgottenPasswordResetTokenEntity;
import org.springframework.http.HttpStatus;

public class InvalidResetTokenException extends BusinessException {

    public InvalidResetTokenException() {
        super("BE - Resetovací odkaz je neplatný nebo expirovaný.", HttpStatus.NOT_FOUND);
    }

    public InvalidResetTokenException(String message) {
        super(message, HttpStatus.GONE);
    }
}
