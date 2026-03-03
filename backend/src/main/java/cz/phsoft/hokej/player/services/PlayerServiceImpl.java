package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementace rozhraní PlayerService ve formě aplikační fasády.
 *
 * Třída poskytuje jednotné vstupní místo pro práci s hráči
 * z pohledu controller vrstvy. Neobsahuje vlastní business logiku,
 * ale zajišťuje orchestrace volání mezi command a query službami.
 *
 * Změnové operace jsou delegovány na PlayerCommandService.
 * Čtecí operace jsou delegovány na PlayerQueryService.
 *
 * Třída tak naplňuje princip oddělení zápisových a čtecích operací
 * a zajišťuje čisté rozhraní pro vyšší aplikační vrstvy.
 */
@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerServiceImpl.class);

    private final PlayerCommandService playerCommandService;
    private final PlayerQueryService playerQueryService;

    /**
     * Vytvoří instanci fasády PlayerService.
     *
     * Závislosti jsou injektovány konstruktorově.
     *
     * @param playerCommandService služba zajišťující změnové operace nad hráči
     * @param playerQueryService služba zajišťující čtecí operace nad hráči
     */
    public PlayerServiceImpl(PlayerCommandService playerCommandService,
                             PlayerQueryService playerQueryService) {
        this.playerCommandService = playerCommandService;
        this.playerQueryService = playerQueryService;
    }

    // CREATE / UPDATE / DELETE

    /**
     * Vytvoří nového hráče.
     *
     * Operace je delegována na PlayerCommandService.
     *
     * @param dto data nového hráče
     * @return vytvořený hráč ve formě PlayerDTO
     */
    @Override
    public PlayerDTO createPlayer(PlayerDTO dto) {
        return playerCommandService.createPlayer(dto);
    }

    /**
     * Vytvoří nového hráče a přiřadí jej ke konkrétnímu uživateli.
     *
     * Operace je delegována na PlayerCommandService.
     *
     * @param dto data nového hráče
     * @param userEmail e-mail uživatele
     * @return vytvořený hráč ve formě PlayerDTO
     */
    @Override
    public PlayerDTO createPlayerForUser(PlayerDTO dto, String userEmail) {
        return playerCommandService.createPlayerForUser(dto, userEmail);
    }

    /**
     * Aktualizuje existujícího hráče.
     *
     * Operace je delegována na PlayerCommandService.
     *
     * @param id identifikátor hráče
     * @param dto nové hodnoty hráče
     * @return aktualizovaný hráč ve formě PlayerDTO
     */
    @Override
    public PlayerDTO updatePlayer(Long id, PlayerDTO dto) {
        return playerCommandService.updatePlayer(id, dto);
    }

    /**
     * Odstraní hráče ze systému.
     *
     * Operace je delegována na PlayerCommandService.
     *
     * @param id identifikátor hráče
     * @return odpověď s výsledkem operace
     */
    @Override
    public SuccessResponseDTO deletePlayer(Long id) {
        return playerCommandService.deletePlayer(id);
    }

    // STATUS – APPROVE / REJECT

    /**
     * Schválí hráče.
     *
     * Operace je delegována na PlayerCommandService.
     *
     * @param id identifikátor hráče
     * @return odpověď s výsledkem operace
     */
    @Override
    public SuccessResponseDTO approvePlayer(Long id) {
        return playerCommandService.approvePlayer(id);
    }

    /**
     * Zamítne hráče.
     *
     * Operace je delegována na PlayerCommandService.
     *
     * @param id identifikátor hráče
     * @return odpověď s výsledkem operace
     */
    @Override
    public SuccessResponseDTO rejectPlayer(Long id) {
        return playerCommandService.rejectPlayer(id);
    }

    /**
     * Změní přiřazeného uživatele k existujícímu hráči.
     *
     * Operace je delegována na PlayerCommandService.
     *
     * @param id identifikátor hráče
     * @param newUserId identifikátor nového uživatele
     */
    @Override
    public void changePlayerUser(Long id, Long newUserId) {
        playerCommandService.changePlayerUser(id, newUserId);
    }

    // READ

    /**
     * Vrátí seznam všech hráčů.
     *
     * Operace je delegována na PlayerQueryService.
     *
     * @return seznam hráčů ve formě PlayerDTO
     */
    @Override
    public List<PlayerDTO> getAllPlayers() {
        return playerQueryService.getAllPlayers();
    }

    /**
     * Vrátí hráče podle jeho identifikátoru.
     *
     * Operace je delegována na PlayerQueryService.
     *
     * @param id identifikátor hráče
     * @return hráč ve formě PlayerDTO
     */
    @Override
    public PlayerDTO getPlayerById(Long id) {
        return playerQueryService.getPlayerById(id);
    }

    /**
     * Vrátí seznam hráčů přiřazených ke konkrétnímu uživateli.
     *
     * Operace je delegována na PlayerQueryService.
     *
     * @param email e-mail uživatele
     * @return seznam hráčů ve formě PlayerDTO
     */
    @Override
    public List<PlayerDTO> getPlayersByUser(String email) {
        return playerQueryService.getPlayersByUser(email);
    }

    // CURRENT PLAYER – SESSION

    /**
     * Nastaví aktuálního hráče pro konkrétního uživatele.
     *
     * Operace je delegována na PlayerCommandService.
     *
     * @param userEmail e-mail uživatele
     * @param playerId identifikátor hráče
     * @return odpověď s výsledkem operace
     */
    @Override
    public SuccessResponseDTO setCurrentPlayerForUser(String userEmail, Long playerId) {
        return playerCommandService.setCurrentPlayerForUser(userEmail, playerId);
    }

    /**
     * Automaticky zvolí aktuálního hráče pro daného uživatele.
     *
     * Operace je delegována na PlayerCommandService.
     *
     * @param userEmail e-mail uživatele
     * @return odpověď s výsledkem operace
     */
    @Override
    public SuccessResponseDTO autoSelectCurrentPlayerForUser(String userEmail) {
        return playerCommandService.autoSelectCurrentPlayerForUser(userEmail);
    }
}