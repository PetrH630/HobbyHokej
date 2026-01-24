package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.security.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
// NEW: implementujeme CurrentPlayerService z balíčku models.services
public class CurrentPlayerServiceImpl implements CurrentPlayerService {

    private final HttpSession session;
    private final PlayerRepository playerRepository;

    public CurrentPlayerServiceImpl(HttpSession session, PlayerRepository playerRepository) {
        this.session = session;
        this.playerRepository = playerRepository;
    }

    @Override
    public Long getCurrentPlayerId() {
        return (Long) session.getAttribute(SessionKeys.CURRENT_PLAYER_ID);
    }

    @Override
    public void setCurrentPlayerId(Long playerId) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));

        if (player.getStatus() != PlayerStatus.APPROVED) {
            // TODO: později nahradíme za BusinessException (např. CurrentPlayerNotAllowedException)
            throw new IllegalStateException(
                    "BE - Nelze zvolit hráče, který není schválen administrátorem."
            );
        }

        session.setAttribute(SessionKeys.CURRENT_PLAYER_ID, playerId);
    }

    @Override
    public void requireCurrentPlayer() {
        if (getCurrentPlayerId() == null) {
            // TODO: později nahradíme za BusinessException (CurrentPlayerNotSetException)
            throw new IllegalStateException("BE - Není zvolen aktuální hráč");
        }
    }

    @Override
    public void clear() {
        session.removeAttribute(SessionKeys.CURRENT_PLAYER_ID);
    }
}
