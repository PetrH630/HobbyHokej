package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.demo.DemoModeOperationNotAllowedException;
import cz.phsoft.hokej.notifications.enums.NotificationType;
import cz.phsoft.hokej.notifications.services.NotificationService;
import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.entities.PlayerSettingsEntity;
import cz.phsoft.hokej.player.enums.PlayerStatus;
import cz.phsoft.hokej.player.exceptions.DuplicateNameSurnameException;
import cz.phsoft.hokej.player.exceptions.InvalidPlayerStatusException;
import cz.phsoft.hokej.player.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.player.mappers.PlayerMapper;
import cz.phsoft.hokej.player.repositories.PlayerRepository;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;
import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.user.enums.PlayerSelectionMode;
import cz.phsoft.hokej.user.exceptions.ForbiddenPlayerAccessException;
import cz.phsoft.hokej.user.exceptions.InvalidChangePlayerUserException;
import cz.phsoft.hokej.user.exceptions.UserNotFoundException;
import cz.phsoft.hokej.user.repositories.AppUserRepository;
import cz.phsoft.hokej.user.services.AppUserSettingsService;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static cz.phsoft.hokej.player.enums.PlayerStatus.APPROVED;
import static cz.phsoft.hokej.player.enums.PlayerStatus.REJECTED;

/**
 * Command služba pro změnové operace nad hráči.
 *
 * Odpovědnosti:
 * - vytváření, aktualizace a mazání hráčů,
 * - změna statusu hráče (approve/reject),
 * - změna vazby hráče na uživatele,
 * - nastavení aktuálního hráče podle uživatele včetně auto-výběru,
 * - odesílání notifikací při změnách.
 *
 * Tato služba neřeší:
 * - HTTP vrstvu a mapování requestů (řeší controllery),
 * - čisté čtecí dotazy (řeší PlayerQueryService),
 * - detailní logiku zápasů (řeší MatchServiceImpl a navazující služby).
 */
@Service
public class PlayerCommandServiceImpl implements PlayerCommandService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerCommandServiceImpl.class);

    @Value("${app.demo-mode:false}")
    private boolean isDemoMode;

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final AppUserRepository appUserRepository;
    private final NotificationService notificationService;
    private final CurrentPlayerService currentPlayerService;
    private final AppUserSettingsService appUserSettingsService;
    private final PlayerSettingsService playerSettingsService;

    /**
     * Vytvoří instanci command služby pro hráče.
     *
     * Závislosti jsou injektovány konstruktorově. Třída se používá
     * jako centrální místo pro veškeré změnové operace nad entitou hráče,
     * včetně odesílání notifikací a správy navazujících nastavení.
     *
     * @param playerRepository repository pro přístup k entitám hráče
     * @param playerMapper mapper pro převod mezi entitou a DTO hráče
     * @param appUserRepository repository pro přístup k uživatelům
     * @param notificationService služba pro odesílání notifikací
     * @param currentPlayerService služba pro správu aktuálního hráče v kontextu
     * @param appUserSettingsService služba pro čtení nastavení uživatele
     * @param playerSettingsService služba pro správu nastavení hráče
     */
    public PlayerCommandServiceImpl(
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


    // CREATE / UPDATE / DELETE


    /**
     * Vytvoří nového hráče bez explicitní vazby na uživatele.
     *
     * Nejprve se ověří unikátnost kombinace jména a příjmení,
     * následně se DTO převede na entitu a uloží do databáze.
     *
     * @param dto data nového hráče
     * @return vytvořený hráč ve formě PlayerDTO
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
     * Vytvoří nového hráče a přiřadí jej ke konkrétnímu uživateli.
     *
     * Metoda ověří existenci uživatele podle e-mailu, zkontroluje
     * unikátnost jména a příjmení hráče, vytvoří novou entitu,
     * nastaví vazbu na uživatele a uloží ji do databáze.
     * Po uložení je odeslána notifikace o vytvoření hráče.
     *
     * @param dto data nového hráče
     * @param userEmail e-mail uživatele, ke kterému má být hráč přiřazen
     * @return vytvořený hráč ve formě PlayerDTO
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
     * Nejprve se načte hráč podle identifikátoru. Pokud se mění jméno
     * nebo příjmení, ověří se unikátnost kombinace. Následně se přepíšou
     * základní údaje hráče i jeho parametry a změna se uloží do databáze.
     * Po uložení je odeslána notifikace o aktualizaci hráče.
     *
     * @param id identifikátor hráče, který má být aktualizován
     * @param dto nové hodnoty hráče
     * @return aktualizovaný hráč ve formě PlayerDTO
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

    /**
     * Smaže hráče z databáze.
     *
     * Metoda nejprve ověří, že hráč existuje, a poté zkontroluje,
     * zda aplikace neběží v demo režimu. V demo režimu je operace
     * odmítnuta pomocí výjimky DemoModeOperationNotAllowedException.
     * Po úspěšném smazání je odeslána notifikace a vrácena
     * odpověď s popisem výsledku.
     *
     * @param id identifikátor hráče, který má být smazán
     * @return odpověď s výsledkem operace
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
     * Schválí hráče a nastaví jeho stav na APPROVED.
     *
     * Metoda deleguje změnu stavu na interní helper changePlayerStatus,
     * který zajistí kontrolu stávajícího stavu, případné vytvoření
     * výchozího nastavení hráče a odeslání notifikace.
     *
     * @param id identifikátor hráče
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
     * Zamítne hráče a nastaví jeho stav na REJECTED.
     *
     * Metoda deleguje změnu stavu na interní helper changePlayerStatus,
     * který zajistí kontrolu stávajícího stavu a odeslání notifikace.
     *
     * @param id identifikátor hráče
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
     * Změní přiřazeného uživatele k existujícímu hráči.
     *
     * Metoda načte hráče i nového uživatele, ověří, že se nemění
     * na stejného uživatele, aktualizuje vazbu hráče na uživatele
     * a uloží změnu do databáze. Následně odešle notifikaci hráči
     * i novému uživateli o změně vazby.
     *
     * @param id identifikátor hráče
     * @param newUserId identifikátor nového uživatele
     */
    @Override
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


    // CURRENT PLAYER – SESSION


    /**
     * Nastaví aktuálního hráče pro uživatele podle e-mailu.
     *
     * Metoda ověří, že hráč existuje a patří danému uživateli.
     * Pokud je kontrola úspěšná, nastaví se identifikátor hráče
     * jako aktuální v CurrentPlayerService a vrátí se odpověď
     * s informací o úspěchu operace.
     *
     * @param userEmail e-mail uživatele
     * @param playerId identifikátor hráče, který má být nastaven jako aktuální
     * @return odpověď s výsledkem operace
     */
    @Override
    @Transactional
    public SuccessResponseDTO setCurrentPlayerForUser(String userEmail, Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);

        assertPlayerBelongsToUser(player, userEmail);

        currentPlayerService.setCurrentPlayerId(playerId);

        String message = "BE - Aktuální hráč nastaven na ID: " + playerId;
        return buildSuccessResponse(message, playerId);
    }

    /**
     * Automaticky vybere aktuálního hráče podle nastavení uživatele.
     *
     * Nejprve se načte nastavení uživatele a určí se režim výběru
     * hráče. Podle hodnoty PlayerSelectionMode se použije
     * odpovídající strategie: autoSelectFirstPlayer nebo
     * autoSelectIfSinglePlayer. Výchozím chováním je volba prvního hráče.
     *
     * @param userEmail e-mail uživatele
     * @return odpověď s výsledkem operace
     */
    @Override
    @Transactional
    public SuccessResponseDTO autoSelectCurrentPlayerForUser(String userEmail) {
        var userSettingsDto = appUserSettingsService.getSettingsForUser(userEmail);

        PlayerSelectionMode mode = PlayerSelectionMode.FIRST_PLAYER;
        if (userSettingsDto.getPlayerSelectionMode() != null) {
            mode = PlayerSelectionMode.valueOf(userSettingsDto.getPlayerSelectionMode());
        }

        return switch (mode) {
            case FIRST_PLAYER -> autoSelectFirstPlayer(userEmail);
            case ALWAYS_CHOOSE -> autoSelectIfSinglePlayer(userEmail);
            default -> autoSelectFirstPlayer(userEmail);
        };
    }

    /**
     * Automaticky vybere prvního hráče uživatele podle ID a nastaví jej jako aktuálního.
     *
     * Pokud uživatel nemá žádného hráče, je vyčištěn aktuální hráč
     * v CurrentPlayerService a je vyhozena výjimka PlayerNotFoundException.
     * Pokud první hráč není schválen, je vyhozena výjimka InvalidPlayerStatusException.
     *
     * @param userEmail e-mail uživatele
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
     * Automaticky vybere aktuálního hráče, pokud má uživatel právě jednoho schváleného hráče.
     *
     * Metoda filtruje hráče uživatele na ty, kteří mají stav APPROVED.
     * Pokud žádný takový hráč neexistuje, je vyhozena výjimka PlayerNotFoundException
     * a aktuální hráč je vymazán. Pokud existuje právě jeden schválený hráč, je nastaven
     * jako aktuální. Pokud je schválených hráčů více, je aktuální hráč vymazán a vrácena
     * odpověď informující, že výběr musí být proveden manuálně.
     *
     * @param userEmail e-mail uživatele
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
     * Najde hráče podle identifikátoru nebo vyhodí výjimku.
     *
     * Metoda slouží jako pomocný wrapper nad repository vrstvou
     * a zajišťuje konzistentní vyhazování výjimky PlayerNotFoundException.
     *
     * @param id identifikátor hráče
     * @return entita hráče
     */
    private PlayerEntity findPlayerOrThrow(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException(id));
    }

    /**
     * Najde uživatele podle identifikátoru nebo vyhodí výjimku.
     *
     * Metoda slouží jako pomocný wrapper nad repository vrstvou
     * a zajišťuje konzistentní vyhazování výjimky UserNotFoundException.
     *
     * @param id identifikátor uživatele
     * @return entita uživatele
     */
    private AppUserEntity findUserOrThrow(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Ověří unikátnost kombinace jména a příjmení hráče.
     *
     * Metoda vyhledá existujícího hráče se stejným jménem a příjmením.
     * Pokud takový hráč existuje a nejde o stejného hráče jako ignoreId,
     * je vyhozena výjimka DuplicateNameSurnameException.
     *
     * @param name jméno hráče
     * @param surname příjmení hráče
     * @param ignoreId identifikátor hráče, který má být při kontrole ignorován
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
     * Ověří, že hráč patří uživateli s daným e-mailem.
     *
     * Pokud hráč nemá přiřazeného uživatele nebo se e-mail neshoduje,
     * je vyhozena výjimka ForbiddenPlayerAccessException.
     *
     * @param player entita hráče
     * @param userEmail e-mail uživatele
     */
    private void assertPlayerBelongsToUser(PlayerEntity player, String userEmail) {
        if (player.getUser() == null ||
                player.getUser().getEmail() == null ||
                !player.getUser().getEmail().equals(userEmail)) {

            throw new ForbiddenPlayerAccessException(player.getId());
        }
    }

    /**
     * Vytvoří instanci SuccessResponseDTO s danou zprávou a identifikátorem.
     *
     * Metoda nastaví současný čas jako čas provedení operace.
     *
     * @param message textová zpráva o výsledku operace
     * @param id identifikátor související entity
     * @return odpověď se stavem úspěchu operace
     */
    private SuccessResponseDTO buildSuccessResponse(String message, Long id) {
        return new SuccessResponseDTO(
                message,
                id,
                LocalDateTime.now().toString()
        );
    }

    /**
     * Změní stav hráče a odešle odpovídající notifikaci.
     *
     * Metoda načte hráče, ověří, že již není v cílovém stavu,
     * nastaví nový stav a v případě schválení hráče vytvoří
     * výchozí nastavení hráče, pokud ještě neexistuje.
     * Následně uloží změnu do databáze, vyhodnotí typ notifikace
     * a případně odešle notifikaci hráči.
     *
     * @param id identifikátor hráče
     * @param targetStatus cílový stav hráče
     * @param alreadyStatus stav, který je považován za již nastavený
     * @param notificationType výchozí typ notifikace (může být přepsán)
     * @param alreadyMessage zpráva použitá ve výjimce při již nastaveném stavu
     * @param successMessageTemplate šablona zprávy pro úspěšnou operaci
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


    /**
     * Odesílá notifikaci hráči.
     *
     * Metoda je tenkým wrapperem nad NotificationService
     * a slouží k centralizaci volání pro hráčské notifikace.
     *
     * @param player hráč, kterému je notifikace určena
     * @param type typ notifikace
     * @param context kontextová data pro sestavení notifikace
     */
    private void notifyPlayer(PlayerEntity player, NotificationType type, Object context) {
        notificationService.notifyPlayer(player, type, context);
    }

    /**
     * Odesílá notifikaci uživateli.
     *
     * Metoda je tenkým wrapperem nad NotificationService
     * a slouží k centralizaci volání pro uživatelské notifikace.
     *
     * @param user uživatel, kterému je notifikace určena
     * @param type typ notifikace
     * @param context kontextová data pro sestavení notifikace
     */
    private void notifyUser(AppUserEntity user, NotificationType type, Object context) {
        notificationService.notifyUser(user, type, context);
    }

    /**
     * Určí typ notifikace podle nového stavu hráče.
     *
     * Pro stavy APPROVED a REJECTED vrací příslušný typ notifikace.
     * Pro ostatní stavy vrací null, což znamená, že se notifikace neodesílá.
     *
     * @param newStatus nový stav hráče
     * @return typ notifikace nebo null, pokud se nemá posílat
     */
    private NotificationType resolveNotificationType(PlayerStatus newStatus) {
        return switch (newStatus) {
            case APPROVED -> NotificationType.PLAYER_APPROVED;
            case REJECTED -> NotificationType.PLAYER_REJECTED;
            default -> null;
        };
    }
}