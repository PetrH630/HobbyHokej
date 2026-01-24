package cz.phsoft.hokej.models.services;

public interface CurrentPlayerService {
    Long getCurrentPlayerId();
    void setCurrentPlayerId(Long playerId);
    void requireCurrentPlayer();
    void clear();

}
