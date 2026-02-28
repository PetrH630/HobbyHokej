package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.entities.PlayerSettingsEntity;
import cz.phsoft.hokej.player.repositories.PlayerRepository;
import cz.phsoft.hokej.player.repositories.PlayerSettingsRepository;
import cz.phsoft.hokej.player.dto.PlayerSettingsDTO;
import cz.phsoft.hokej.player.mappers.PlayerSettingsMapper;
import cz.phsoft.hokej.player.exceptions.PlayerNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementace služby pro práci s nastavením hráče (PlayerSettingsEntity).
 *
 * Odpovědnosti:
 * - načítání nastavení hráče podle jeho ID,
 * - vytváření výchozího nastavení pro hráče, pokud ještě neexistuje,
 * - aktualizace existujícího nastavení podle PlayerSettingsDTO.
 *
 * Tato třída:
 * - neřeší autorizaci ani ověřování vlastnictví hráče (řeší controller),
 * - neodesílá notifikace, pouze spravuje data v databázi,
 * - spolupracuje s:
 *   - PlayerRepository pro ověření existence hráče,
 *   - PlayerSettingsRepository pro práci s nastavením,
 *   - PlayerSettingsMapper pro mapování mezi entitou a DTO.
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

        // pro jistotu se zajišťuje navázání na hráče
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

    /**
     * Vytvoří výchozí nastavení pro daného hráče.
     *
     * Výchozí chování:
     * - kontaktní email a telefon jsou ponechány prázdné (null),
     * - emailové notifikace:
     *   - notifyOnRegistration = true,
     *   - notifyOnExcuse = true,
     *   - notifyOnMatchChange = true,
     *   - notifyOnMatchCancel = true,
     *   - notifyOnPayment = false,
     * - připomínky:
     *   - notifyReminders = true,
     *   - reminderHoursBefore = 24,
     * - herní preference pro automatické přesuny:
     *   - possibleMoveToAnotherTeam = false,
     *   - possibleChangePlayerPosition = false.
     */
    @Override
    public PlayerSettingsEntity createDefaultSettingsForPlayer(PlayerEntity player) {
        PlayerSettingsEntity settings = new PlayerSettingsEntity();

        settings.setPlayer(player);

        // kontakty
        settings.setContactEmail(null);
        settings.setContactPhone(null);

        // kanály
        settings.setEmailEnabled(true);
        settings.setSmsEnabled(false);

        // typy notifikací
        settings.setNotifyOnRegistration(true);
        settings.setNotifyOnExcuse(true);
        settings.setNotifyOnMatchChange(true);
        settings.setNotifyOnMatchCancel(true);
        settings.setNotifyOnPayment(false);

        // připomínky
        settings.setNotifyReminders(false);
        settings.setReminderHoursBefore(24);

        // herní preference – výchozí: žádné automatické přesuny
        settings.setPossibleMoveToAnotherTeam(false);
        settings.setPossibleChangePlayerPosition(false);

        return settings;
    }

}