package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.DuplicateNameSurnameException;
import cz.phsoft.hokej.exceptions.InvalidPlayerStatusException;
import cz.phsoft.hokej.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.exceptions.UserNotFoundException;
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

    public PlayerServiceImpl(
            PlayerRepository playerRepository,
            PlayerMapper playerMapper,
            AppUserRepository appUserRepository,
            NotificationService notificationService // NEW
    ) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
        this.appUserRepository = appUserRepository;
        this.notificationService = notificationService; // NEW
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


}