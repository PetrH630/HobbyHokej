package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.*;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;
import cz.phsoft.hokej.models.dto.mappers.PlayerMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.services.NotificationService;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final AppUserRepository appUserRepository;
    private final NotificationService notificationService;
    private final CurrentPlayerService currentPlayerService;       // NEW

    public PlayerServiceImpl(
            PlayerRepository playerRepository,
            PlayerMapper playerMapper,
            AppUserRepository appUserRepository,
            NotificationService notificationService,
            CurrentPlayerService currentPlayerService             // NEW
    ) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
        this.appUserRepository = appUserRepository;
        this.notificationService = notificationService;
        this.currentPlayerService = currentPlayerService;         // NEW
    }

    @Override
    public List<PlayerDTO> getAllPlayers() {
        return playerRepository.findAll().stream()
                .map(playerMapper::toDTO)
                .toList();
    }

    @Override
    public PlayerDTO getPlayerById(Long id) {
        PlayerEntity player = playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException(id)); // místo RuntimeException
        return playerMapper.toDTO(player);
    }


    // --- TRANSACTIONAL pro zápis dat ---
    @Override
    @Transactional
    public PlayerDTO createPlayer(PlayerDTO dto) {
        checkDuplicateNameSurname(dto.getName(), dto.getSurname(), null);

        PlayerEntity entity = playerMapper.toEntity(dto);
        PlayerEntity saved = playerRepository.save(entity);

        // notificationService.notifyPlayer(saved, NotificationType.PLAYER_CREATED, null);

        return playerMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public PlayerDTO createPlayerForUser(PlayerDTO dto, String userEmail) {
        AppUserEntity user = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        checkDuplicateNameSurname(dto.getName(), dto.getSurname(), null);

        PlayerEntity player = playerMapper.toEntity(dto);
        player.setUser(user); // přiřazení hráče k uživateli


        PlayerEntity saved = playerRepository.save(player);

        notificationService.notifyPlayer(saved, NotificationType.PLAYER_CREATED, null);

        return playerMapper.toDTO(saved);
    }

    @Override
    public List<PlayerDTO> getPlayersByUser(String email) {
        return playerRepository.findByUser_EmailOrderByIdAsc(email).stream()
                .map(playerMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public PlayerDTO updatePlayer(Long id, PlayerDTO dto) {
        PlayerEntity existing = findPlayerOrThrow(id);

        // pokud se jméno/příjmení mění, ověř duplicitu
        if (!existing.getName().equals(dto.getName())
                || !existing.getSurname().equals(dto.getSurname())) {
            checkDuplicateNameSurname(dto.getName(), dto.getSurname(), id);
        }

        existing.setName(dto.getName());
        existing.setSurname(dto.getSurname());
        existing.setNickname(dto.getNickName());
        existing.setPhoneNumber(dto.getPhoneNumber());
        existing.setType(dto.getType());
        existing.setTeam(dto.getTeam());
        existing.setStatus(dto.getStatus());

        PlayerEntity saved = playerRepository.save(existing);

        notificationService.notifyPlayer(saved, NotificationType.PLAYER_UPDATED, null);

        return playerMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public SuccessResponseDTO deletePlayer(Long id) {
        PlayerEntity player = findPlayerOrThrow(id);
        playerRepository.delete(player);

        return new SuccessResponseDTO(
                "Hráč " + player.getFullName() + " byl úspěšně smazán",
                id,
                LocalDateTime.now().toString()
        );
    }

    // --- privátní metoda pro kontrolu duplicity jména a příjmení ---
    private void checkDuplicateNameSurname(String name, String surname, Long ignoreId) {
        Optional<PlayerEntity> duplicateOpt = playerRepository.findByNameAndSurname(name, surname);

        if (duplicateOpt.isPresent()) {
            if (ignoreId == null || !duplicateOpt.get().getId().equals(ignoreId)) {
                throw new DuplicateNameSurnameException(name, surname);
            }
        }
    }

    private PlayerEntity findPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    @Override
    @Transactional
    public SuccessResponseDTO approvePlayer(Long id) {
        PlayerEntity player = findPlayerOrThrow(id);

        if (player.getStatus() == PlayerStatus.APPROVED) {
            throw new InvalidPlayerStatusException("BE - Hráč už je schválen.");
        }
        player.setStatus(PlayerStatus.APPROVED);
        playerRepository.save(player);

        notificationService.notifyPlayer(player, NotificationType.PLAYER_APPROVED, null);

        return new SuccessResponseDTO(
                "Hráč " + player.getFullName() + " byl úspěšně aktivován",
                id,
                LocalDateTime.now().toString()
        );
    }

    @Override
    @Transactional
    public SuccessResponseDTO rejectPlayer(Long id) {
        PlayerEntity player = findPlayerOrThrow(id);

        if (player.getStatus() == PlayerStatus.REJECTED) {
            throw new InvalidPlayerStatusException("BE - Hráč už je zamítnut.");
        }
        player.setStatus(PlayerStatus.REJECTED);
        playerRepository.save(player);

        notificationService.notifyPlayer(player, NotificationType.PLAYER_REJECTED, null);

        return new SuccessResponseDTO(
                "Hráč " + player.getFullName() + " byl úspěšně zamítnut",
                id,
                LocalDateTime.now().toString()
        );
    }

    // ----------------------------------------------------------------------
    // NEW – nastavení aktuálního hráče pro přihlášeného uživatele
    // ----------------------------------------------------------------------
    @Override
    public SuccessResponseDTO setCurrentPlayerForUser(String userEmail, Long playerId) {

        // 1) Najdu hráče
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));

        // 2) Ověřím, že patří uživateli s daným emailem
        if (player.getUser() == null
                || player.getUser().getEmail() == null
                || !player.getUser().getEmail().equals(userEmail)) {

            // CHANGED: místo InvalidPlayerStatusException použijeme jasnější doménovou chybu
            throw new ForbiddenPlayerAccessException(playerId);
        }

        // 3) Deleguji na CurrentPlayerService (ten už hlídá PlayerStatus.APPROVED)
        currentPlayerService.setCurrentPlayerId(playerId);

        // 4) Vrátím jednotný SuccessResponseDTO
        return new SuccessResponseDTO(
                "BE - Aktuální hráč nastaven na ID: " + playerId,
                playerId,
                LocalDateTime.now().toString()
        );
    }

    // ----------------------------------------------------------------------
    // NEW – automatický výběr aktuálního hráče po loginu
    // ----------------------------------------------------------------------
    @Override
    public SuccessResponseDTO autoSelectCurrentPlayerForUser(String userEmail) {

        // 1) Vezmu hráče přes existující metodu getPlayersByUser
        List<PlayerDTO> players = getPlayersByUser(userEmail);

        if (players.size() == 1) {
            PlayerDTO player = players.get(0);

            // 2) Nastavím ho jako aktuálního
            currentPlayerService.setCurrentPlayerId(player.getId());

            // 3) Vrátím SuccessResponseDTO s ID
            return new SuccessResponseDTO(
                    "BE - Automaticky nastaven aktuální hráč na ID: " + player.getId(),
                    player.getId(),
                    LocalDateTime.now().toString()
            );
        }

        // 4) Uživatel má 0 nebo více hráčů – necháme výběr na FE
        return new SuccessResponseDTO(
                "BE - Uživatel má více (nebo žádné) hráče, je nutný ruční výběr.",
                null,
                LocalDateTime.now().toString()
        );
    }
}


