package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerSettingsEntity;
import cz.phsoft.hokej.models.dto.PlayerSettingsDTO;

/**
 * Service rozhraní pro práci s nastavením hráče ({@link PlayerSettingsEntity}).
 *
 * Odpovědnosti:
 * - poskytovat přístup k nastavení konkrétního hráče,
 * - vytvářet výchozí nastavení hráče,
 * - aktualizovat existující nastavení hráče na základě DTO.
 *
 * Architektura:
 * - pracuje s {@link PlayerSettingsDTO} jako přenosovým objektem mezi backendem a frontendem,
 * - nezajišťuje autorizaci ani kontrolu vlastnictví hráče,
 *   tyto kontroly se provádějí v controlleru nebo ve vyšší servisní vrstvě.
 */
public interface PlayerSettingsService {

    /**
     * Vrátí nastavení pro hráče podle jeho ID.
     *
     * Pokud nastavení ještě neexistuje, vytvoří se z výchozích hodnot
     * a uloží se pro daného hráče. Tím se zajišťuje, že volající vždy
     * obdrží platné nastavení.
     *
     * @param playerId ID hráče
     * @return nastavení hráče ve formě {@link PlayerSettingsDTO}
     */
    PlayerSettingsDTO getSettingsForPlayer(Long playerId);

    /**
     * Aktualizuje nastavení pro hráče podle jeho ID.
     *
     * Pokud hráč ještě nemá nastavení, vytvoří se výchozí nastavení
     * a následně se na něj aplikují hodnoty z DTO.
     *
     * @param playerId ID hráče
     * @param dto      nové hodnoty nastavení
     * @return aktualizované nastavení ve formě {@link PlayerSettingsDTO}
     */
    PlayerSettingsDTO updateSettingsForPlayer(Long playerId, PlayerSettingsDTO dto);

    /**
     * Vytvoří výchozí nastavení pro hráče.
     *
     * Metoda pouze vytváří instanci {@link PlayerSettingsEntity}
     * s nastavovanými default hodnotami. Uložení do databáze
     * je odpovědností volajícího kódu.
     *
     * @param player hráč, ke kterému budou defaultní hodnoty přiřazeny
     * @return nová instance {@link PlayerSettingsEntity} s výchozím nastavením
     */
    PlayerSettingsEntity createDefaultSettingsForPlayer(PlayerEntity player);
}
