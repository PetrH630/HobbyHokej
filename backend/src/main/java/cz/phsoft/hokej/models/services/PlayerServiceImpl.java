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

import static cz.phsoft.hokej.data.enums.PlayerStatus.APPROVED;
import static cz.phsoft.hokej.data.enums.PlayerStatus.REJECTED;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service vrstva pro práci s hráči ({@link PlayerEntity}).
 *
 * Tato třída zodpovídá za:
 * - provádění CRUD operací nad hráči,
 * - kontrolu duplicity jména a příjmení,
 * - správu vazby hráče na uživatele ({@link AppUserEntity}),
 * - změnu statusu hráče (APPROVED, REJECTED) včetně spuštění notifikací,
 * - nastavení aktuálního hráče v {@link CurrentPlayerService}
 *   pro přihlášeného uživatele.
 *
 * Tato třída neřeší:
 * - HTTP vrstvu, session a mapování requestů a response (řeší controllery),
 * - autentizaci a autorizaci (řeší Spring Security a controller vrstva),
 * - detailní logiku zápasů (řeší {@link MatchServiceImpl} a navazující služby).
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
     * Vytvoří nového hráče bez vazby na uživatele.
     *
     * Před uložením se kontroluje duplicita kombinace jména a příjmení.
     * Po vytvoření se notifikace neodesílá, aby bylo zachováno původní chování.
     *
     * @param dto data nového hráče
     * @return vytvořený hráč ve formě {@link PlayerDTO}
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
     * Vytvoří nového hráče a přiřadí jej k uživateli podle emailu.
     *
     * Postup:
     * - vyhledá se uživatel podle emailu,
     * - ověří se, že neexistuje jiný hráč se stejným jménem a příjmením,
     * - namapuje se {@link PlayerDTO} na {@link PlayerEntity},
     * - nastaví se vazba na uživatele a hráč se uloží,
     * - odešle se notifikace {@link NotificationType#PLAYER_CREATED}.
     *
     * @param dto       data nového hráče
     * @param userEmail email uživatele, ke kterému má být hráč přiřazen
     * @return vytvořený hráč ve formě {@link PlayerDTO}
     * @throws UserNotFoundException         pokud uživatel neexistuje
     * @throws DuplicateNameSurnameException pokud existuje hráč se stejným jménem a příjmením
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
     * Při změně jména nebo příjmení se kontroluje duplicita kombinace jméno + příjmení.
     * Poté se přepíší základní údaje hráče a odešle se notifikace
     * {@link NotificationType#PLAYER_UPDATED}.
     *
     * @param id  ID hráče
     * @param dto nové hodnoty hráče
     * @return aktualizovaný hráč ve formě {@link PlayerDTO}
     * @throws PlayerNotFoundException       pokud hráč neexistuje
     * @throws DuplicateNameSurnameException pokud nová kombinace jméno + příjmení koliduje
     *                                       s jiným hráčem
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
        if (dto.getPlayerStatus() != null) {
            existing.setPlayerStatus(dto.getPlayerStatus());
        }

        PlayerEntity saved = playerRepository.save(existing);
        notifyPlayer(saved, NotificationType.PLAYER_UPDATED, saved);

        return playerMapper.toDTO(saved);
    }

    /**
     * Smaže hráče podle ID.
     *
     * Po smazání je odeslána notifikace {@link NotificationType#PLAYER_DELETED}.
     *
     * @param id ID hráče
     * @return {@link SuccessResponseDTO} s potvrzující zprávou
     * @throws PlayerNotFoundException pokud hráč neexistuje
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

   // STATUS – APPROVE / REJECT
    /**
     * Schválí hráče a nastaví mu status {@link PlayerStatus#APPROVED}.
     *
     * Pokud je hráč již schválen, vyhodí se výjimka
     * {@link InvalidPlayerStatusException}. Po schválení se odešle
     * notifikace {@link NotificationType#PLAYER_APPROVED}.
     *
     * @param id ID hráče
     * @return odpověď s výsledkem operace
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
     * Zamítne hráče a nastaví mu status {@link PlayerStatus#REJECTED}.
     *
     * Pokud je hráč již zamítnut, vyhodí se výjimka
     * {@link InvalidPlayerStatusException}. Po zamítnutí se odešle
     * notifikace {@link NotificationType#PLAYER_REJECTED}.
     *
     * @param id ID hráče
     * @return odpověď s výsledkem operace
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
     * Změní přiřazeného uživatele k hráči a odešle notifikace
     * o změně vazby hráče a uživatele.
     *
     * Při pokusu převést hráče na stejného uživatele se vyhodí
     * {@link InvalidChangePlayerUserException}.
     *
     * @param id        ID hráče
     * @param newUserId ID nového uživatele
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
    // READ
   /**
     * Vrátí všechny hráče v systému.
     *
     * @return seznam hráčů ve formě {@link PlayerDTO}
     */
    @Override
    public List<PlayerDTO> getAllPlayers() {
        return playerRepository.findAll().stream()
                .map(playerMapper::toDTO)
                .toList();
    }

    /**
     * Vrátí hráče podle ID.
     *
     * @param id ID hráče
     * @return {@link PlayerDTO} odpovídající hráči
     * @throws PlayerNotFoundException pokud hráč s daným ID neexistuje
     */
    @Override
    public PlayerDTO getPlayerById(Long id) {
        PlayerEntity player = findPlayerOrThrow(id);
        return playerMapper.toDTO(player);
    }

    /**
     * Vrátí všechny hráče přiřazené uživateli s daným emailem.
     *
     * Hráči jsou vráceni v pořadí podle ID vzestupně.
     *
     * @param email email uživatele
     * @return seznam hráčů ve formě {@link PlayerDTO}
     */
    @Override
    public List<PlayerDTO> getPlayersByUser(String email) {
        return playerRepository.findByUser_EmailOrderByIdAsc(email).stream()
                .map(playerMapper::toDTO)
                .toList();
    }

    // CURRENT PLAYER – SESSION

    /**
     * Nastaví aktuálního hráče pro daného uživatele.
     *
     * Nejprve se ověří, že hráč existuje a patří danému uživateli
     * (podle emailu). Poté se jeho ID předá do
     * {@link CurrentPlayerService#setCurrentPlayerId(Long)}.
     *
     * @param userEmail email přihlášeného uživatele
     * @param playerId  ID hráče, který má být nastaven jako aktuální
     * @return odpověď s výsledkem operace
     * @throws PlayerNotFoundException        pokud hráč neexistuje
     * @throws ForbiddenPlayerAccessException pokud hráč nepatří danému uživateli
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
     * Automaticky vybere aktuálního hráče pro uživatele podle
     * nastavení v AppUserSettings (playerSelectionMode).
     *
     * Režimy:
     * - FIRST_PLAYER: vždy se vybere první hráč podle ID,
     * - ALWAYS_CHOOSE: automaticky se vybere pouze v případě,
     *   že má uživatel právě jednoho schváleného hráče.
     *
     * @param userEmail email uživatele
     * @return odpověď s výsledkem operace
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
     * @param userEmail email uživatele
     * @return odpověď s výsledkem operace
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
     * @param userEmail email uživatele
     * @return odpověď s výsledkem operace
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

    // PRIVATE HELPERY – ENTITY / DUPLICITY

    /**
     * Najde hráče podle ID nebo vyhodí {@link PlayerNotFoundException}.
     *
     * @param id ID hráče
     * @return entita hráče
     */
    private PlayerEntity findPlayerOrThrow(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException(id));
    }

    /**
     * Najde uživatele podle ID nebo vyhodí {@link UserNotFoundException}.
     *
     * @param id ID uživatele
     * @return entita uživatele
     */
    private AppUserEntity findUserOrThrow(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Zajistí unikátnost kombinace jméno + příjmení.
     *
     * Při vytváření nového hráče se očekává ignoreId null.
     * Při aktualizaci se ignoruje hráč se stejným ID, aby nebyl
     * považován za duplicitního sám se sebou.
     *
     * @param name     jméno hráče
     * @param surname  příjmení hráče
     * @param ignoreId ID hráče, který má být ignorován, nebo null
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
     * Ověří, že hráč patří danému uživateli (podle emailu).
     *
     * Pokud hráč uživateli nepatří, vyhodí se {@link ForbiddenPlayerAccessException}.
     *
     * @param player    hráč
     * @param userEmail email uživatele
     */
    private void assertPlayerBelongsToUser(PlayerEntity player, String userEmail) {
        if (player.getUser() == null ||
                player.getUser().getEmail() == null ||
                !player.getUser().getEmail().equals(userEmail)) {

            throw new ForbiddenPlayerAccessException(player.getId());
        }
    }

    /**
     * Vytvoří standardizovanou úspěšnou odpověď {@link SuccessResponseDTO}
     * s danou zprávou a ID.
     *
     * @param message textová zpráva
     * @param id      ID entity, které se operace týkala
     * @return úspěšná odpověď
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
     * Ověří se, že hráč již není v cílovém stavu, případně se nastaví
     * výchozí nastavení pro hráče při schválení. Po uložení se odešle
     * notifikace podle nového statusu.
     *
     * @param id                     ID hráče
     * @param targetStatus           cílový status
     * @param alreadyStatus          status, který znamená „už je v tomto stavu“
     * @param notificationType       typ notifikace (případně přepsaný podle statusu)
     * @param alreadyMessage         zpráva pro případ, že je hráč již v cílovém stavu
     * @param successMessageTemplate šablona úspěšné zprávy
     * @return odpověď s výsledkem operace
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

    // PRIVÁTNÍ HELPERY – NOTIFIKACE

    private void notifyPlayer(PlayerEntity player, NotificationType type, Object context) {
        notificationService.notifyPlayer(player, type, context);
    }

    private void notifyUser(AppUserEntity user, NotificationType type, Object context) {
        notificationService.notifyUser(user, type, context);
    }

    /**
     * Převede status hráče na typ notifikace.
     *
     * Pokud se pro zadaný status notifikace neposílá,
     * vrací se null. Status PENDING se zde nevyužívá,
     * protože notifikace při vytvoření hráče jsou řešeny jinde.
     *
     * @param newStatus nový status hráče
     * @return odpovídající {@link NotificationType} nebo null
     */
    private NotificationType resolveNotificationType(PlayerStatus newStatus) {
        return switch (newStatus) {
            case APPROVED -> NotificationType.PLAYER_APPROVED;
            case REJECTED -> NotificationType.MATCH_REGISTRATION_CANCELED;
            default -> null;
        };
    }

}
