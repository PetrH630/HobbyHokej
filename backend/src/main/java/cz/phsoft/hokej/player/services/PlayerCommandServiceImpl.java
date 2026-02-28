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
import jakarta.transaction.Transactional;
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

    // ======================
    // CREATE / UPDATE / DELETE
    // ======================

    @Override
    @Transactional
    public PlayerDTO createPlayer(PlayerDTO dto) {
        ensureUniqueNameSurname(dto.getName(), dto.getSurname(), null);

        PlayerEntity entity = playerMapper.toEntity(dto);
        PlayerEntity saved = playerRepository.save(entity);

        return playerMapper.toDTO(saved);
    }

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

    // ======================
    // CURRENT PLAYER – SESSION
    // ======================

    @Override
    @Transactional
    public SuccessResponseDTO setCurrentPlayerForUser(String userEmail, Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);

        assertPlayerBelongsToUser(player, userEmail);

        currentPlayerService.setCurrentPlayerId(playerId);

        String message = "BE - Aktuální hráč nastaven na ID: " + playerId;
        return buildSuccessResponse(message, playerId);
    }

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

    private PlayerEntity findPlayerOrThrow(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException(id));
    }

    private AppUserEntity findUserOrThrow(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private void ensureUniqueNameSurname(String name, String surname, Long ignoreId) {
        Optional<PlayerEntity> duplicateOpt = playerRepository.findByNameAndSurname(name, surname);

        if (duplicateOpt.isPresent()) {
            PlayerEntity duplicate = duplicateOpt.get();

            if (ignoreId == null || !duplicate.getId().equals(ignoreId)) {
                throw new DuplicateNameSurnameException(name, surname);
            }
        }
    }

    private void assertPlayerBelongsToUser(PlayerEntity player, String userEmail) {
        if (player.getUser() == null ||
                player.getUser().getEmail() == null ||
                !player.getUser().getEmail().equals(userEmail)) {

            throw new ForbiddenPlayerAccessException(player.getId());
        }
    }

    private SuccessResponseDTO buildSuccessResponse(String message, Long id) {
        return new SuccessResponseDTO(
                message,
                id,
                LocalDateTime.now().toString()
        );
    }

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

    private NotificationType resolveNotificationType(PlayerStatus newStatus) {
        return switch (newStatus) {
            case APPROVED -> NotificationType.PLAYER_APPROVED;
            case REJECTED -> NotificationType.PLAYER_REJECTED;
            default -> null;
        };
    }
}