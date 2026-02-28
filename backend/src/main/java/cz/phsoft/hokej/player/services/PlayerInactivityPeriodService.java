package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.dto.PlayerInactivityPeriodDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Rozhraní pro správu období neaktivity hráčů.
 *
 * Tato service definuje kontrakt pro práci s časovými úseky,
 * ve kterých je hráč považován za neaktivního
 * (zranění, dovolená, dlouhodobá absence a podobné situace).
 *
 * Odpovědnosti:
 * - eviduje období, kdy se hráč nemůže účastnit zápasů,
 * - poskytuje přehledy období neaktivity pro konkrétního hráče i pro administraci,
 * - umožňuje ověření, zda je hráč v daném okamžiku aktivní.
 *
 * Tato service:
 * - pracuje s DTO {@link PlayerInactivityPeriodDTO}, nikoliv přímo s entitami,
 * - odděluje business logiku neaktivity od persistence vrstvy.
 *
 * Tato service neřeší:
 * - autorizaci a role uživatelů,
 * - notifikace,
 * - UI logiku.
 */
public interface PlayerInactivityPeriodService {

    /**
     * Vrátí seznam všech období neaktivity v systému.
     *
     * Typicky se používá v administrátorských přehledech.
     *
     * @return seznam všech období neaktivity ve formě DTO
     */
    List<PlayerInactivityPeriodDTO> getAll();

    /**
     * Vrátí období neaktivity podle jeho ID.
     *
     * @param id ID období neaktivity
     * @return období neaktivity ve formě DTO
     */
    PlayerInactivityPeriodDTO getById(Long id);

    /**
     * Vrátí seznam období neaktivity pro konkrétního hráče.
     *
     * @param playerId ID hráče
     * @return seznam období neaktivity daného hráče
     */
    List<PlayerInactivityPeriodDTO> getByPlayer(Long playerId);

    /**
     * Vytvoří nové období neaktivity hráče.
     *
     * Implementace je zodpovědná za:
     * - validaci časového rozsahu (začátek před koncem),
     * - kontrolu překryvů s existujícími obdobími neaktivity.
     *
     * @param dto data nového období neaktivity
     * @return vytvořené období neaktivity ve formě DTO
     */
    PlayerInactivityPeriodDTO create(PlayerInactivityPeriodDTO dto);

    /**
     * Aktualizuje existující období neaktivity.
     *
     * Implementace je zodpovědná za:
     * - validaci časového rozsahu,
     * - kontrolu překryvů s ostatními obdobími neaktivity daného hráče.
     *
     * @param id  ID období neaktivity, které má být upraveno
     * @param dto nové hodnoty období neaktivity
     * @return aktualizované období neaktivity ve formě DTO
     */
    PlayerInactivityPeriodDTO update(Long id, PlayerInactivityPeriodDTO dto);

    /**
     * Odstraní období neaktivity podle ID.
     *
     * @param id ID období neaktivity, které má být smazáno
     */
    void delete(Long id);

    /**
     * Ověří, zda je hráč v daném okamžiku aktivní.
     *
     * Metoda vrací informaci, zda se zadaný čas
     * nenachází v žádném z evidovaných období neaktivity hráče.
     *
     * Typické použití:
     * - při registraci hráče na zápas,
     * - při validaci účasti hráče v konkrétním čase,
     * - při filtrování dostupných zápasů pro hráče.
     *
     * @param player   hráč, jehož aktivita se ověřuje
     * @param dateTime časový okamžik, pro který se aktivita kontroluje
     * @return true, pokud je hráč v daném čase aktivní, jinak false
     */
    boolean isActive(PlayerEntity player, LocalDateTime dateTime);
}
