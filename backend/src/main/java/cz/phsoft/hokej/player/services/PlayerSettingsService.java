package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.entities.PlayerSettingsEntity;
import cz.phsoft.hokej.player.dto.PlayerSettingsDTO;

/**
 * Servisní rozhraní pro práci s nastavením hráče.
 *
 * Odpovědnost:
 * - poskytování přístupu k nastavení konkrétního hráče,
 * - vytváření výchozího nastavení hráče,
 * - aktualizace existujícího nastavení hráče na základě DTO objektu.
 *
 * Architektura:
 * - pracuje s objektem PlayerSettingsDTO jako přenosovou reprezentací
 *   mezi backendem a frontendem,
 * - neprovádí autorizaci ani kontrolu vlastnictví hráče; tyto kontroly
 *   se provádějí ve vyšších vrstvách (controller nebo jiná service).
 */
public interface PlayerSettingsService {

    /**
     * Vrátí nastavení pro hráče podle jeho identifikátoru.
     *
     * Pokud nastavení ještě neexistuje, vytvoří se z výchozích hodnot
     * a uloží se pro daného hráče. Volající tak vždy obdrží platné
     * nastavení hráče.
     *
     * @param playerId identifikátor hráče
     * @return nastavení hráče ve formě PlayerSettingsDTO
     */
    PlayerSettingsDTO getSettingsForPlayer(Long playerId);

    /**
     * Aktualizuje nastavení pro hráče podle jeho identifikátoru.
     *
     * Pokud hráč ještě nemá nastavení, vytvoří se nejprve výchozí
     * nastavení a následně se na něj aplikují hodnoty z DTO objektu.
     *
     * @param playerId identifikátor hráče
     * @param dto nové hodnoty nastavení hráče
     * @return aktualizované nastavení ve formě PlayerSettingsDTO
     */
    PlayerSettingsDTO updateSettingsForPlayer(Long playerId, PlayerSettingsDTO dto);

    /**
     * Vytvoří výchozí nastavení pro hráče.
     *
     * Metoda pouze vytvoří novou instanci PlayerSettingsEntity
     * s nastavenými defaultními hodnotami. Uložení této entity
     * do databáze je odpovědností volajícího kódu.
     *
     * @param player hráč, ke kterému budou výchozí hodnoty přiřazeny
     * @return nová instance PlayerSettingsEntity s výchozím nastavením
     */
    PlayerSettingsEntity createDefaultSettingsForPlayer(PlayerEntity player);
}