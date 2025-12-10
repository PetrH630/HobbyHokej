package cz.phsoft.hokej.exceptions;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(Long playerId) {
        super("Hráč s ID " + playerId + " nenalezen.");}
}
