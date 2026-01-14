package cz.phsoft.hokej.security;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class CurrentPlayerService {

    private final HttpSession session;

    public CurrentPlayerService(HttpSession session) {
        this.session = session;
    }

    public Long getCurrentPlayerId() {
        return (Long) session.getAttribute(SessionKeys.CURRENT_PLAYER_ID);
    }

    public void setCurrentPlayerId(Long playerId) {
        session.setAttribute(SessionKeys.CURRENT_PLAYER_ID, playerId);
    }

    public void requireCurrentPlayer() {
        if (getCurrentPlayerId() == null) {
            throw new IllegalStateException("Není zvolen aktuální hráč");
        }
    }

    public void clear() {
        session.removeAttribute(SessionKeys.CURRENT_PLAYER_ID);
    }
}
