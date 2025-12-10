package cz.phsoft.hokej.exceptions;

public class MatchNotFoundException extends RuntimeException {
    public MatchNotFoundException(Long matchId) {
        super("Zápas s ID " + matchId + " nenalezen.");;
    }
}
