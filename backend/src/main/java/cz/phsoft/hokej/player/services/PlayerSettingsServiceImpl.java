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
 * Implementace služby pro práci s nastavením hráče.
 *
 * Odpovědnost:
 * - načítání nastavení hráče podle jeho identifikátoru,
 * - vytváření výchozího nastavení pro hráče, pokud ještě neexistuje,
 * - aktualizace existujícího nastavení na základě DTO objektu.
 *
 * Třída:
 * - neřeší autorizaci ani kontrolu vlastnictví hráče,
 * - neodesílá notifikace a obsahuje pouze datovou logiku,
 * - spolupracuje s repository vrstvou pro práci s entitami,
 * - využívá mapper pro převod mezi entitou a DTO.
 */
@Service
@Transactional
public class PlayerSettingsServiceImpl implements PlayerSettingsService {

    private final PlayerRepository playerRepository;
    private final PlayerSettingsRepository playerSettingsRepository;
    private final PlayerSettingsMapper mapper;

    /**
     * Vytvoří instanci služby pro práci s nastavením hráče.
     *
     * Závislosti jsou injektovány konstruktorově.
     *
     * @param playerRepository repository pro přístup k entitám hráče
     * @param playerSettingsRepository repository pro práci s nastavením hráče
     * @param mapper mapper pro převod mezi entitou a DTO
     */
    public PlayerSettingsServiceImpl(PlayerRepository playerRepository,
                                     PlayerSettingsRepository playerSettingsRepository,
                                     PlayerSettingsMapper mapper) {
        this.playerRepository = playerRepository;
        this.playerSettingsRepository = playerSettingsRepository;
        this.mapper = mapper;
    }

    /**
     * Vrátí nastavení pro hráče podle jeho identifikátoru.
     *
     * Pokud nastavení ještě neexistuje, vytvoří se výchozí nastavení,
     * uloží se do databáze a následně se vrátí ve formě DTO.
     *
     * @param playerId identifikátor hráče
     * @return nastavení hráče ve formě PlayerSettingsDTO
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
     * Aktualizuje nastavení pro hráče podle jeho identifikátoru.
     *
     * Pokud hráč ještě nemá uložené nastavení, vytvoří se výchozí
     * nastavení a následně se na něj aplikují hodnoty z DTO objektu.
     * Před uložením je zajištěno navázání nastavení na hráče.
     *
     * @param playerId identifikátor hráče
     * @param dto nové hodnoty nastavení
     * @return aktualizované nastavení ve formě PlayerSettingsDTO
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


    // HELPER METODY


    /**
     * Najde hráče podle identifikátoru nebo vyhodí výjimku.
     *
     * Metoda slouží jako pomocný wrapper nad repository vrstvou
     * a zajišťuje jednotné vyhazování výjimky PlayerNotFoundException.
     *
     * @param playerId identifikátor hráče
     * @return entita hráče
     */
    private PlayerEntity findPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    /**
     * Vytvoří výchozí nastavení pro daného hráče.
     *
     * Výchozí hodnoty:
     * - kontaktní e-mail a telefon nejsou nastaveny,
     * - e-mailové notifikace jsou povoleny, SMS notifikace jsou zakázány,
     * - notifikace o registraci, omluvě, změně zápasu a zrušení zápasu jsou povoleny,
     * - notifikace o platbě jsou zakázány,
     * - připomínky jsou ve výchozím stavu vypnuty,
     * - připomínka před zápasem je nastavena na 24 hodin,
     * - automatické přesuny do jiného týmu a změna pozice jsou povoleny.
     *
     * Metoda pouze vytvoří instanci entity bez jejího uložení do databáze.
     *
     * @param player hráč, ke kterému bude nastavení přiřazeno
     * @return nová instance PlayerSettingsEntity s výchozím nastavením
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
        settings.setPossibleMoveToAnotherTeam(true);
        settings.setPossibleChangePlayerPosition(true);

        return settings;
    }

}