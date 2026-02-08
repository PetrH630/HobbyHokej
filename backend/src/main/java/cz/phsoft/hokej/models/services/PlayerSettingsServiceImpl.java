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
 * Implementace služby pro práci s nastavením hráče ({@link PlayerSettingsEntity}).
 *
 * Odpovědnosti:
 * - načítání nastavení hráče podle jeho ID,
 * - vytváření výchozího nastavení pro hráče, pokud ještě neexistuje,
 * - aktualizace existujícího nastavení podle {@link PlayerSettingsDTO}.
 *
 * Tato třída:
 * - neřeší autorizaci ani ověřování vlastnictví hráče (řeší controller),
 * - neodesílá notifikace, pouze spravuje data v databázi,
 * - spolupracuje s:
 *   - {@link PlayerRepository} pro ověření existence hráče,
 *   - {@link PlayerSettingsRepository} pro práci s nastavením,
 *   - {@link PlayerSettingsMapper} pro mapování mezi entitou a DTO.
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

    /**
     * Vrátí nastavení pro hráče podle jeho ID.
     *
     * Postup:
     * - ověří se existence hráče,
     * - pokusí se načíst existující nastavení hráče,
     * - pokud neexistuje žádný záznam, vytvoří se výchozí nastavení
     *   pomocí {@link #createDefaultSettingsForPlayer(PlayerEntity)} a uloží se,
     * - výsledek se namapuje na {@link PlayerSettingsDTO}.
     *
     * @param playerId ID hráče
     * @return nastavení hráče ve formě {@link PlayerSettingsDTO}
     * @throws PlayerNotFoundException pokud hráč neexistuje
     */
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

    /**
     * Aktualizuje nastavení hráče podle jeho ID.
     *
     * Postup:
     * - ověří se existence hráče,
     * - načte se existující nastavení hráče, nebo se vytvoří nové výchozí,
     * - na entitu se aplikují hodnoty z {@link PlayerSettingsDTO},
     * - zajišťuje se navázání na hráče (settings.setPlayer),
     * - entita se uloží a navrátí se ve formě DTO.
     *
     * @param playerId ID hráče
     * @param dto      nové hodnoty nastavení
     * @return aktualizované nastavení ve formě {@link PlayerSettingsDTO}
     * @throws PlayerNotFoundException pokud hráč neexistuje
     */
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

    /**
     * Najde hráče podle ID nebo vyhodí {@link PlayerNotFoundException}.
     *
     * Metoda centralizuje práci s {@link PlayerRepository} a
     * zjednodušuje obsluhu chyb při neexistujícím hráči.
     *
     * @param playerId ID hráče
     * @return entita hráče
     * @throws PlayerNotFoundException pokud hráč s daným ID neexistuje
     */
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
     *   - reminderHoursBefore = 24.
     *
     * Default hodnoty odpovídají původní logice, která byla dříve
     * uložena přímo v entitě {@link PlayerEntity} (emailEnabled, smsEnabled)
     * a nyní je přesunuta do dedikované entity {@link PlayerSettingsEntity}.
     *
     * Metoda pouze vrací neinicializovanou entitu, uložení do databáze
     * provádí volající kód.
     *
     * @param player hráč, pro kterého se výchozí nastavení vytváří
     * @return nová instance {@link PlayerSettingsEntity} s výchozím nastavením
     */
    @Override
    public PlayerSettingsEntity createDefaultSettingsForPlayer(PlayerEntity player) {
        PlayerSettingsEntity settings = new PlayerSettingsEntity();
        settings.setPlayer(player);

        // explicitně nastavené default hodnoty

        settings.setContactEmail(null);
        settings.setContactPhone(null);

        // původní logika z PlayerEntity.emailEnabled / smsEnabled přesunuta do nastavení hráče
        settings.setEmailEnabled(false);
        settings.setSmsEnabled(false);
        settings.setNotifyOnRegistration(true);
        settings.setNotifyOnExcuse(true);
        settings.setNotifyOnMatchChange(true);
        settings.setNotifyOnMatchCancel(true);
        settings.setNotifyOnPayment(false);

        settings.setNotifyReminders(false);
        settings.setReminderHoursBefore(24);

        return settings;
    }

}
