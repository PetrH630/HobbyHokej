package cz.phsoft.hokej.exceptions;

public class InvalidPlayerStatusException extends RuntimeException {
    public InvalidPlayerStatusException(String message) {
        super(message);
    }
}
