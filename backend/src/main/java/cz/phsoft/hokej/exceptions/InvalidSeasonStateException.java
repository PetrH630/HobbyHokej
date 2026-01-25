package cz.phsoft.hokej.exceptions;

public class InvalidSeasonStateException extends RuntimeException {
    public InvalidSeasonStateException(String message) {
        super(message);
    }
}
