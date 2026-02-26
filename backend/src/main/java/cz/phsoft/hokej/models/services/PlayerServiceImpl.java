package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerSettingsEntity;
import cz.phsoft.hokej.data.enums.PlayerSelectionMode;
import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.*;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;
import cz.phsoft.hokej.models.mappers.PlayerMapper;
import cz.phsoft.hokej.models.services.notification.NotificationService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import cz.phsoft.hokej.models.dto.PlayerDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static cz.phsoft.hokej.data.enums.PlayerStatus.APPROVED;
import static cz.phsoft.hokej.data.enums.PlayerStatus.REJECTED;

/**
 * Service vrstva pro práci s hráči ({@link PlayerEntity}).
 *
 * Odpovědnosti:
 * - provádění CRUD operací nad hráči,
 * - kontrola duplicity jména a příjmení,
 * - správa vazby hráče na uživatele ({@link AppUserEntity}),
 * - změna statusu hráče (APPROVED, REJECTED) včetně spuštění notifikací,
 * - nastavení aktuálního hráče v {@link CurrentPlayerService} pro přihlášeného uživatele.
 *
 * Tato třída neřeší:
 * - HTTP vrstvu, session a mapování requestů a response (řeší controllery),
 * - autentizaci a autorizaci (řeší Spring Security a controller vrstva),
 * - detailní logiku zápasů (řeší {@link MatchServiceImpl} a navazující služby).
 *
 * Notifikace se delegují do {@link NotificationService}.
 */
@Service
public class PlayerServiceImpl implements PlayerService {

    @Value("${app.demo-mode:false}")
    private boolean isDemoMode;

    private static final Logger logger = LoggerFactory.getLogger(PlayerServiceImpl.class);

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final AppUserRepository appUserRepository;
    private final NotificationService notificationService;
    private final CurrentPlayerService currentPlayerService;
    private final AppUserSettingsService appUserSettingsService;
    private final PlayerSettingsService playerSettingsService;

    public PlayerServiceImpl(
            PlayerRepository playerRepository,
            PlayerMapper playerMapper,
            AppUserRepository appUserRepository,
            NotificationService notificationService,
            CurrentPlayerService currentPlayerService,
            AppUserSettingsService appUserSettingsService,
            PlayerSettingsService playerSettingsService
    ) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
        this.appUserRepository = appUserRepository;
        this.notificationService = notificationService;
        this.currentPlayerService = currentPlayerService;
        this.appUserSettingsService = appUserSettingsService;
        this.playerSettingsService = playerSettingsService;
    }

    // ======================
    // CREATE / UPDATE / DELETE
    // ======================

    /**
     * Vytváří nového hráče bez vazby na uživatele.
     *
     * Postup:
     * - ověří se unikátnost kombinace jméno + příjmení,
     * - převede se {@link PlayerDTO} na {@link PlayerEntity} pomocí {@link PlayerMapper},
     * - entita se uloží do databáze přes {@link PlayerRepository},
     * - výsledek se namapuje zpět do {@link PlayerDTO}.
     *
     * Notifikace se po vytvoření neodesílají, aby bylo zachováno původní chování.
     *
     * @param dto Data nového hráče.
     * @return Vytvořený hráč ve formě {@link PlayerDTO}.
     * @throws DuplicateNameSurnameException Pokud již existuje hráč se stejným jménem a příjmením.
     */
    @Override
    @Transactional
    public PlayerDTO createPlayer(PlayerDTO dto) {
        ensureUniqueNameSurname(dto.getName(), dto.getSurname(), null);

        PlayerEntity entity = playerMapper.toEntity(dto);
        PlayerEntity saved = playerRepository.save(entity);

        return playerMapper.toDTO(saved);
    }

    /**
     * Vytváří nového hráče a přiřazuje jej k uživateli podle e-mailu.
     *
     * Postup:
     * - vyhledá se uživatel podle e-mailu,
     * - ověří se unikátnost kombinace jméno + příjmení,
     * - namapuje se {@link PlayerDTO} na {@link PlayerEntity},
     * - nastaví se vazba na uživatele,
     * - entita se uloží do databáze,
     * - odešle se notifikace {@link NotificationType#PLAYER_CREATED}.
     *
     * @param dto       Data nového hráče.
     * @param userEmail E-mail uživatele, ke kterému má být hráč přiřazen.
     * @return Vytvořený hráč ve formě {@link PlayerDTO}.
     * @throws UserNotFoundException         Pokud uživatel s daným e-mailem neexistuje.
     * @throws DuplicateNameSurnameException Pokud již existuje jiný hráč se stejným jménem a příjmením.
     */
    @Override
    @Transactional
    public PlayerDTO createPlayerForUser(PlayerDTO dto, String userEmail) {
        AppUserEntity user = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        ensureUniqueNameSurname(dto.getName(), dto.getSurname(), null);

        PlayerEntity player = playerMapper.toEntity(dto);
        player.setUser(user);
        PlayerEntity saved = playerRepository.save(player);

        notifyPlayer(saved, NotificationType.PLAYER_CREATED, saved);

        return playerMapper.toDTO(saved);
    }

    /**
     * Aktualizuje existujícího hráče.
     *
     * Postup:
     * - načte se hráč podle ID,
     * - pokud se mění jméno nebo příjmení, ověří se unikátnost kombinace jméno + příjmení,
     * - přepíšou se základní údaje (jméno, příjmení, přezdívka, telefon, typ, tým, pozice),
     * - status hráče se změní pouze v případě, že je v DTO explicitně uveden,
     * - entita se uloží do databáze,
     * - odešle se notifikace {@link NotificationType#PLAYER_UPDATED}.
     *
     * @param id  ID hráče.
     * @param dto Nové hodnoty hráče.
     * @return Aktualizovaný hráč ve formě {@link PlayerDTO}.
     * @throws PlayerNotFoundException       Pokud hráč neexistuje.
     * @throws DuplicateNameSurnameException Pokud nová kombinace jméno + příjmení koliduje s jiným hráčem.
     */
    @Override
    @Transactional
    public PlayerDTO updatePlayer(Long id, PlayerDTO dto) {
        PlayerEntity existing = findPlayerOrThrow(id);

        boolean nameChanged =
                !existing.getName().equals(dto.getName()) ||
                        !existing.getSurname().equals(dto.getSurname());

        if (nameChanged) {
            ensureUniqueNameSurname(dto.getName(), dto.getSurname(), id);
        }

        existing.setName(dto.getName());
        existing.setSurname(dto.getSurname());
        existing.setNickname(dto.getNickname());
        existing.setPhoneNumber(dto.getPhoneNumber());
        existing.setType(dto.getType());
        existing.setTeam(dto.getTeam());
        existing.setPrimaryPosition(dto.getPrimaryPosition());
        existing.setSecondaryPosition(dto.getSecondaryPosition());

        if (dto.getPlayerStatus() != null) {
            existing.setPlayerStatus(dto.getPlayerStatus());
        }

        PlayerEntity saved = playerRepository.save(existing);
        notifyPlayer(saved, NotificationType.PLAYER_UPDATED, saved);

        return playerMapper.toDTO(saved);
    }

    // TODO - Nebude probíhat mazání hráče - bude nastaven statut ARCHIVED
    /**
     * Maže hráče podle ID.
     *
     * Před smazáním se ověřuje demo režim. V demo režimu se operace zablokuje
     * vyhozením {@link DemoModeOperationNotAllowedException}. Při úspěšném smazání
     * se odešle notifikace {@link NotificationType#PLAYER_DELETED} a vrací se
     * standardizovaná odpověď {@link SuccessResponseDTO}.
     *
     * @param id ID hráče.
     * @return Odpověď s potvrzující zprávou.
     * @throws PlayerNotFoundException             Pokud hráč neexistuje.
     * @throws DemoModeOperationNotAllowedException Pokud aplikace běží v demo režimu.
     */
    @Override
    @Transactional
    public SuccessResponseDTO deletePlayer(Long id) {
        PlayerEntity player = findPlayerOrThrow(id);

        if (isDemoMode) {
            throw new DemoModeOperationNotAllowedException(
                    "Hráč nebude smazán. Aplikace běží v DEMO režimu."
            );
        }

        playerRepository.delete(player);

        String message = "Hráč " + player.getFullName() + " byl úspěšně smazán";
        notifyPlayer(player, NotificationType.PLAYER_DELETED, player);

        return buildSuccessResponse(message, id);
    }

    // ======================
    // STATUS – APPROVE / REJECT
    // ======================

    /**
     * Schvaluje hráče a nastavuje mu status {@link PlayerStatus#APPROVED}.
     *
     * Provedení:
     * - ověří se, že hráč již není ve stavu APPROVED,
     * - nastaví se status APPROVED,
     * - pokud hráč ještě nemá nastavení, vytvoří se výchozí nastavení přes
     *   {@link PlayerSettingsService#createDefaultSettingsForPlayer(PlayerEntity)},
     * - hráč se uloží a odešle se notifikace {@link NotificationType#PLAYER_APPROVED}.
     *
     * @param id ID hráče.
     * @return Odpověď s výsledkem operace.
     * @throws InvalidPlayerStatusException Pokud je hráč již ve stavu APPROVED.
     */
    @Override
    @Transactional
    public SuccessResponseDTO approvePlayer(Long id) {
        return changePlayerStatus(
                id,
                PlayerStatus.APPROVED,
                PlayerStatus.APPROVED,
                NotificationType.PLAYER_APPROVED,
                "BE - Hráč už je schválen.",
                "Hráč %s byl úspěšně aktivován"
        );
    }

    /**
     * Zamítá hráče a nastavuje mu status {@link PlayerStatus#REJECTED}.
     *
     * Po úspěšné změně stavu se odešle notifikace
     * {@link NotificationType#PLAYER_REJECTED}.
     *
     * @param id ID hráče.
     * @return Odpověď s výsledkem operace.
     * @throws InvalidPlayerStatusException Pokud je hráč již ve stavu REJECTED.
     */
    @Override
    @Transactional
    public SuccessResponseDTO rejectPlayer(Long id) {
        return changePlayerStatus(
                id,
                REJECTED,
                REJECTED,
                NotificationType.PLAYER_REJECTED,
                "BE - Hráč už je zamítnut.",
                "Hráč %s byl úspěšně zamítnut"
        );
    }

    /**
     * Mění přiřazeného uživatele k hráči a odesílá notifikace
     * o změně vazby hráče a uživatele.
     *
     * Postup:
     * - načte se hráč a nový uživatel,
     * - ověří se, že se nepokouší převést hráče na stejného uživatele,
     * - nastaví se nový uživatel do entitiy hráče a entita se uloží,
     * - odešle se notifikace hráči i uživateli typu {@link NotificationType#PLAYER_CHANGE_USER}.
     *
     * @param id        ID hráče.
     * @param newUserId ID nového uživatele.
     * @throws InvalidChangePlayerUserException Pokud je nový uživatel shodný s původním.
     * @throws PlayerNotFoundException          Pokud hráč neexistuje.
     * @throws UserNotFoundException            Pokud uživatel neexistuje.
     */
    @Transactional
    public void changePlayerUser(Long id, Long newUserId) {
        PlayerEntity player = findPlayerOrThrow(id);
        AppUserEntity newUser = findUserOrThrow(newUserId);
        AppUserEntity oldUser = player.getUser();

        if (oldUser != null && oldUser.getId().equals(newUserId)) {
            throw new InvalidChangePlayerUserException();
        }

        player.setUser(newUser);
        PlayerEntity saved = playerRepository.save(player);

        notifyPlayer(saved, NotificationType.PLAYER_CHANGE_USER, newUser);
        notifyUser(newUser, NotificationType.PLAYER_CHANGE_USER, player);
    }

    // ======================
    // READ
    // ======================

    /**
     * Vrací všechny hráče v systému.
     *
     * Data se načítají z {@link PlayerRepository} a převádí se do {@link PlayerDTO}
     * pomocí {@link PlayerMapper}.
     *
     * @return Seznam hráčů ve formě {@link PlayerDTO}.
     */
    @Override
    public List<PlayerDTO> getAllPlayers() {
        return playerRepository.findAll().stream()
                .map(playerMapper::toDTO)
                .toList();
    }

    /**
     * Vrací hráče podle ID.
     *
     * @param id ID hráče.
     * @return {@link PlayerDTO} odpovídající hráči.
     * @throws PlayerNotFoundException Pokud hráč s daným ID neexistuje.
     */
    @Override
    public PlayerDTO getPlayerById(Long id) {
        PlayerEntity player = findPlayerOrThrow(id);
        return playerMapper.toDTO(player);
    }

    /**
     * Vrací všechny hráče přiřazené uživateli s daným e-mailem.
     *
     * Hráči jsou vráceni v pořadí podle ID vzestupně.
     *
     * @param email E-mail uživatele.
     * @return Seznam hráčů ve formě {@link PlayerDTO}.
     */
    @Override
    public List<PlayerDTO> getPlayersByUser(String email) {
        return playerRepository.findByUser_EmailOrderByIdAsc(email).stream()
                .map(playerMapper::toDTO)
                .toList();
    }

    // ======================
    // CURRENT PLAYER – SESSION
    // ======================

    /**
     * Nastavuje aktuálního hráče pro daného uživatele.
     *
     * Postup:
     * - načte se hráč podle ID,
     * - ověří se, že hráč patří danému uživateli (podle e-mailu),
     * - ID hráče se předá do {@link CurrentPlayerService#setCurrentPlayerId(Long)}.
     *
     * @param userEmail E-mail přihlášeného uživatele.
     * @param playerId  ID hráče, který má být nastaven jako aktuální.
     * @return Odpověď s výsledkem operace.
     * @throws PlayerNotFoundException        Pokud hráč neexistuje.
     * @throws ForbiddenPlayerAccessException Pokud hráč nepatří danému uživateli.
     */
    @Override
    public SuccessResponseDTO setCurrentPlayerForUser(String userEmail, Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);

        assertPlayerBelongsToUser(player, userEmail);

        currentPlayerService.setCurrentPlayerId(playerId);

        String message = "BE - Aktuální hráč nastaven na ID: " + playerId;
        return buildSuccessResponse(message, playerId);
    }

    /**
     * Automaticky vybírá aktuálního hráče pro uživatele podle
     * nastavení v AppUserSettings (playerSelectionMode).
     *
     * Režimy:
     * - FIRST_PLAYER: vždy se vybere první hráč podle ID,
     * - ALWAYS_CHOOSE: automaticky se vybere pouze v případě,
     *   že má uživatel právě jednoho schváleného hráče.
     *
     * @param userEmail E-mail uživatele.
     * @return Odpověď s výsledkem operace.
     * @throws PlayerNotFoundException Pokud uživatel nemá žádného relevantního hráče.
     */
    @Override
    public SuccessResponseDTO autoSelectCurrentPlayerForUser(String userEmail) {

        var userSettingsDto = appUserSettingsService.getSettingsForUser(userEmail);

        PlayerSelectionMode mode = PlayerSelectionMode.FIRST_PLAYER;
        if (userSettingsDto.getPlayerSelectionMode() != null) {
            mode = PlayerSelectionMode.valueOf(userSettingsDto.getPlayerSelectionMode());
        }

        switch (mode) {
            case FIRST_PLAYER:
                return autoSelectFirstPlayer(userEmail);
            case ALWAYS_CHOOSE:
                return autoSelectIfSinglePlayer(userEmail);
            default:
                return autoSelectFirstPlayer(userEmail);
        }
    }

    /**
     * Pomocná metoda pro režim FIRST_PLAYER.
     *
     * Najde prvního hráče uživatele (podle ID) a nastaví ho
     * jako aktuálního hráče v {@link CurrentPlayerService}.
     * Pokud uživatel nemá žádného hráče nebo hráč není schválen,
     * vyhodí se výjimka.
     *
     * @param userEmail E-mail uživatele.
     * @return Odpověď s výsledkem operace.
     * @throws PlayerNotFoundException     Pokud uživatel nemá žádného hráče.
     * @throws InvalidPlayerStatusException Pokud první hráč není ve stavu APPROVED.
     */
    private SuccessResponseDTO autoSelectFirstPlayer(String userEmail) {
        List<PlayerEntity> players = playerRepository.findByUser_EmailOrderByIdAsc(userEmail);

        if (players.isEmpty()) {
            currentPlayerService.clear();
            throw new PlayerNotFoundException(
                    "BE - Uživatel nemá přiřazeného žádného hráče. Nelze automaticky vybrat.",
                    userEmail
            );
        }

        PlayerEntity firstPlayer = players.get(0);
        if (firstPlayer.getPlayerStatus() != APPROVED) {
            throw new InvalidPlayerStatusException(
                    "BE - Nelze zvolit hráče, který není schválen administrátorem."
            );
        }

        currentPlayerService.setCurrentPlayerId(firstPlayer.getId());
        String message = "BE - Automaticky byl vybrán první hráč: " + firstPlayer.getFullName();
        return buildSuccessResponse(message, firstPlayer.getId());
    }

    /**
     * Pomocná metoda pro režim ALWAYS_CHOOSE.
     *
     * Pokud má uživatel právě jednoho schváleného hráče, tento hráč
     * se nastaví jako aktuální. V ostatních případech se current player
     * vyčistí a očekává se ruční výběr na frontendu.
     *
     * @param userEmail E-mail uživatele.
     * @return Odpověď s výsledkem operace.
     * @throws PlayerNotFoundException Pokud uživatel nemá žádného schváleného hráče.
     */
    private SuccessResponseDTO autoSelectIfSinglePlayer(String userEmail) {
        List<PlayerEntity> players = playerRepository
                .findByUser_EmailOrderByIdAsc(userEmail).stream()
                .filter(p -> p.getPlayerStatus() == APPROVED)
                .toList();

        if (players.isEmpty()) {
            currentPlayerService.clear();

            throw new PlayerNotFoundException(
                    "BE - Uživatel nemá přiřazeného žádného hráče schváleného Administrátorem. Nelze automaticky vybrat.",
                    userEmail
            );
        }
        if (players.size() == 1) {
            PlayerEntity onlyPlayer = players.get(0);

            currentPlayerService.setCurrentPlayerId(onlyPlayer.getId());

            String message = "BE - Byl vybrán jediný schválený hráč: " + onlyPlayer.getFullName();
            return buildSuccessResponse(message, onlyPlayer.getId());
        }

        currentPlayerService.clear();

        StringBuilder sb = new StringBuilder();
        for (PlayerEntity player : players) {
            sb.append(players.indexOf(player) + 1);
            sb.append(". - ");
            sb.append(player.getFullName());
            sb.append(" / ");
        }
        String message = "BE - Uživatel má více hráčů a musí je vybrat manuálně dle nastavení: " + sb;
        return buildSuccessResponse(message, 0L);
    }

    // ======================
    // PRIVATE HELPERY – ENTITY / DUPLICITY
    // ======================

    /**
     * Načítá hráče podle ID nebo vyhazuje {@link PlayerNotFoundException}.
     *
     * @param id ID hráče.
     * @return Entita hráče.
     */
    private PlayerEntity findPlayerOrThrow(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException(id));
    }

    /**
     * Načítá uživatele podle ID nebo vyhazuje {@link UserNotFoundException}.
     *
     * @param id ID uživatele.
     * @return Entita uživatele.
     */
    private AppUserEntity findUserOrThrow(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Zajišťuje unikátnost kombinace jméno + příjmení.
     *
     * Při vytváření nového hráče se očekává ignoreId null.
     * Při aktualizaci se ignoruje hráč se stejným ID, aby nebyl
     * považován za duplicitního sám se sebou.
     *
     * @param name     Jméno hráče.
     * @param surname  Příjmení hráče.
     * @param ignoreId ID hráče, který má být ignorován, nebo null.
     */
    private void ensureUniqueNameSurname(String name, String surname, Long ignoreId) {
        Optional<PlayerEntity> duplicateOpt = playerRepository.findByNameAndSurname(name, surname);

        if (duplicateOpt.isPresent()) {
            PlayerEntity duplicate = duplicateOpt.get();

            if (ignoreId == null || !duplicate.getId().equals(ignoreId)) {
                throw new DuplicateNameSurnameException(name, surname);
            }
        }
    }

    /**
     * Ověřuje, že hráč patří danému uživateli (podle e-mailu).
     *
     * Pokud hráč uživateli nepatří, vyhodí se {@link ForbiddenPlayerAccessException}.
     *
     * @param player    Hráč.
     * @param userEmail E-mail uživatele.
     */
    private void assertPlayerBelongsToUser(PlayerEntity player, String userEmail) {
        if (player.getUser() == null ||
                player.getUser().getEmail() == null ||
                !player.getUser().getEmail().equals(userEmail)) {

            throw new ForbiddenPlayerAccessException(player.getId());
        }
    }

    /**
     * Vytváří standardizovanou úspěšnou odpověď {@link SuccessResponseDTO}
     * s danou zprávou a ID.
     *
     * @param message Textová zpráva.
     * @param id      ID entity, které se operace týkala.
     * @return Úspěšná odpověď.
     */
    private SuccessResponseDTO buildSuccessResponse(String message, Long id) {
        return new SuccessResponseDTO(
                message,
                id,
                LocalDateTime.now().toString()
        );
    }

    /**
     * Obecná pomocná metoda pro změnu statusu hráče (approve/reject).
     *
     * Postup:
     * - ověří se, že hráč již není v cílovém stavu,
     * - nastaví se nový status,
     * - v případě schválení se zajistí výchozí nastavení hráče,
     * - uloží se entita a vyhodnotí se typ notifikace podle nového statusu,
     * - odešle se notifikace hráči, pokud je typ notifikace definován.
     *
     * @param id                     ID hráče.
     * @param targetStatus           Cílový status.
     * @param alreadyStatus          Status, který znamená „už je v tomto stavu“.
     * @param notificationType       Výchozí typ notifikace (případně přepsaný podle statusu).
     * @param alreadyMessage         Zpráva pro případ, že je hráč již v cílovém stavu.
     * @param successMessageTemplate Šablona úspěšné zprávy.
     * @return Odpověď s výsledkem operace.
     */
    private SuccessResponseDTO changePlayerStatus(Long id,
                                                  PlayerStatus targetStatus,
                                                  PlayerStatus alreadyStatus,
                                                  NotificationType notificationType,
                                                  String alreadyMessage,
                                                  String successMessageTemplate) {

        PlayerEntity player = findPlayerOrThrow(id);

        if (player.getPlayerStatus() == alreadyStatus) {
            throw new InvalidPlayerStatusException(alreadyMessage);
        }

        player.setPlayerStatus(targetStatus);

        if (targetStatus == APPROVED && player.getSettings() == null) {
            PlayerSettingsEntity settings =
                    playerSettingsService.createDefaultSettingsForPlayer(player);
            player.setSettings(settings);
        }
        PlayerEntity saved = playerRepository.save(player);

        notificationType = resolveNotificationType(targetStatus);
        if (notificationType != null) {
            notifyPlayer(saved, notificationType, saved);
        }

        String message = String.format(successMessageTemplate, saved.getFullName());
        return buildSuccessResponse(message, id);
    }

    // ======================
    // PRIVÁTNÍ HELPERY – NOTIFIKACE
    // ======================

    private void notifyPlayer(PlayerEntity player, NotificationType type, Object context) {
        notificationService.notifyPlayer(player, type, context);
    }

    private void notifyUser(AppUserEntity user, NotificationType type, Object context) {
        notificationService.notifyUser(user, type, context);
    }

    /**
     * Převádí status hráče na typ notifikace.
     *
     * Pokud se pro zadaný status notifikace neposílá,
     * vrací se null. Status PENDING se zde nevyužívá,
     * protože notifikace při vytvoření hráče jsou řešeny jinde.
     *
     * @param newStatus Nový status hráče.
     * @return Odpovídající {@link NotificationType} nebo null.
     */
    private NotificationType resolveNotificationType(PlayerStatus newStatus) {
        return switch (newStatus) {
            case APPROVED -> NotificationType.PLAYER_APPROVED;
            case REJECTED -> NotificationType.PLAYER_REJECTED;
            default -> null;
        };
    }

}