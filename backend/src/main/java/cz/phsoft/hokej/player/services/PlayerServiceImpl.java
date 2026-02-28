package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Fasáda pro práci s hráči.
 *
 * Odpovědnost:
 * - poskytuje jednotné rozhraní {@link PlayerService} pro controllery,
 * - deleguje změnové operace do {@link PlayerCommandService},
 * - deleguje čtecí operace do {@link PlayerQueryService}.
 *
 * Fasáda sama neobsahuje business logiku, pouze orchestrace.
 */
@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerServiceImpl.class);

    private final PlayerCommandService playerCommandService;
    private final PlayerQueryService playerQueryService;

    public PlayerServiceImpl(PlayerCommandService playerCommandService,
                             PlayerQueryService playerQueryService) {
        this.playerCommandService = playerCommandService;
        this.playerQueryService = playerQueryService;
    }

    // ======================
    // CREATE / UPDATE / DELETE
    // ======================

    @Override
    public PlayerDTO createPlayer(PlayerDTO dto) {
        return playerCommandService.createPlayer(dto);
    }

    @Override
    public PlayerDTO createPlayerForUser(PlayerDTO dto, String userEmail) {
        return playerCommandService.createPlayerForUser(dto, userEmail);
    }

    @Override
    public PlayerDTO updatePlayer(Long id, PlayerDTO dto) {
        return playerCommandService.updatePlayer(id, dto);
    }

    @Override
    public SuccessResponseDTO deletePlayer(Long id) {
        return playerCommandService.deletePlayer(id);
    }

    // ======================
    // STATUS – APPROVE / REJECT
    // ======================

    @Override
    public SuccessResponseDTO approvePlayer(Long id) {
        return playerCommandService.approvePlayer(id);
    }

    @Override
    public SuccessResponseDTO rejectPlayer(Long id) {
        return playerCommandService.rejectPlayer(id);
    }

    @Override
    public void changePlayerUser(Long id, Long newUserId) {
        playerCommandService.changePlayerUser(id, newUserId);
    }

    // ======================
    // READ
    // ======================

    @Override
    public List<PlayerDTO> getAllPlayers() {
        return playerQueryService.getAllPlayers();
    }

    @Override
    public PlayerDTO getPlayerById(Long id) {
        return playerQueryService.getPlayerById(id);
    }

    @Override
    public List<PlayerDTO> getPlayersByUser(String email) {
        return playerQueryService.getPlayersByUser(email);
    }

    // ======================
    // CURRENT PLAYER – SESSION
    // ======================

    @Override
    public SuccessResponseDTO setCurrentPlayerForUser(String userEmail, Long playerId) {
        return playerCommandService.setCurrentPlayerForUser(userEmail, playerId);
    }

    @Override
    public SuccessResponseDTO autoSelectCurrentPlayerForUser(String userEmail) {
        return playerCommandService.autoSelectCurrentPlayerForUser(userEmail);
    }
}