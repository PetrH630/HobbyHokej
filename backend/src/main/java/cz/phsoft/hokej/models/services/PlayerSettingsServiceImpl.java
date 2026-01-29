package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerSettingsEntity;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.data.repositories.PlayerSettingsRepository;
import cz.phsoft.hokej.models.dto.PlayerSettingsDTO;
import cz.phsoft.hokej.models.mappers.PlayerSettingsMapper;
import cz.phsoft.hokej.exceptions.PlayerNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementace service pro práci s nastavením hráče.
 */
@Service
@Transactional
public class PlayerSettingsServiceImpl implements PlayerSettingsService {

    private final PlayerRepository playerRepository;
    private final PlayerSettingsRepository playerSettingsRepository;
    private final PlayerSettingsMapper mapper;

    public PlayerSettingsServiceImpl(PlayerRepository playerRepository,
                                     PlayerSettingsRepository playerSettingsRepository,
                                     PlayerSettingsMapper mapper) {
        this.playerRepository = playerRepository;
        this.playerSettingsRepository = playerSettingsRepository;
        this.mapper = mapper;
    }

    @Override
    public PlayerSettingsDTO getSettingsForPlayer(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);

        Optional<PlayerSettingsEntity> existingOpt =
                playerSettingsRepository.findByPlayer(player);

        PlayerSettingsEntity settings = existingOpt.orElseGet(() -> {
            PlayerSettingsEntity created = createDefaultSettingsForPlayer(player);
            return playerSettingsRepository.save(created);
        });

        return mapper.toDTO(settings);
    }

    @Override
    public PlayerSettingsDTO updateSettingsForPlayer(Long playerId, PlayerSettingsDTO dto) {
        PlayerEntity player = findPlayerOrThrow(playerId);

        PlayerSettingsEntity settings = playerSettingsRepository.findByPlayer(player)
                .orElseGet(() -> createDefaultSettingsForPlayer(player));

        mapper.updateEntityFromDTO(dto, settings);

        // pro jistotu napojení
        settings.setPlayer(player);

        PlayerSettingsEntity saved = playerSettingsRepository.save(settings);

        return mapper.toDTO(saved);
    }

    // =========================
    // HELPER METODY
    // =========================

    private PlayerEntity findPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }
    @Override
    public PlayerSettingsEntity createDefaultSettingsForPlayer(PlayerEntity player) {
        PlayerSettingsEntity settings = new PlayerSettingsEntity();
        settings.setPlayer(player);

        // defaulty – stejné jako v entitě, ale explicitně

        settings.setContactEmail(null);
        settings.setContactPhone(null);

        // POZOR: tady je přesun staré logiky z PlayerEntity.emailEnabled / smsEnabled
        // Původně jsi měl tyto příznaky v PlayerEntity – teď žijí tady:
        settings.setNotifyOnRegistration(true);
        settings.setNotifyOnExcuse(true);
        settings.setNotifyOnMatchChange(true);
        settings.setNotifyOnMatchCancel(true);
        settings.setNotifyOnPayment(false);

        settings.setNotifyReminders(true);
        settings.setReminderHoursBefore(24);

        return settings;
    }


}
