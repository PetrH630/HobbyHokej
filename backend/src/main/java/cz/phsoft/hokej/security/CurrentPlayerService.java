package cz.phsoft.hokej.security;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.PlayerNotFoundException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class CurrentPlayerService {

    private final HttpSession session;
    private final PlayerRepository playerRepository;

    public CurrentPlayerService(HttpSession session, PlayerRepository playerRepository) {
        this.session = session;
        this.playerRepository = playerRepository;
    }

    public Long getCurrentPlayerId() {
        return (Long) session.getAttribute(SessionKeys.CURRENT_PLAYER_ID);
    }

    public void setCurrentPlayerId(Long playerId) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));

        if (player.getStatus() != PlayerStatus.APPROVED) {
            throw new IllegalStateException(
                    "BE - Nelze zvolit hráče, který není schválen administrátorem."
            );
        }

        session.setAttribute(SessionKeys.CURRENT_PLAYER_ID, playerId);
    }

    public void requireCurrentPlayer() {
        if (getCurrentPlayerId() == null) {
            throw new IllegalStateException("BE - Není zvolen aktuální hráč");
        }
    }

    public void clear() {
        session.removeAttribute(SessionKeys.CURRENT_PLAYER_ID);
    }
}
