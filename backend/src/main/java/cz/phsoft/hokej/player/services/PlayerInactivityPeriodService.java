package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.dto.PlayerInactivityPeriodDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Rozhraní pro správu období neaktivity hráčů.
 *
 * Definuje kontrakt pro práci s časovými úseky, ve kterých je hráč
 * považován za neaktivního (zranění, dovolená, dlouhodobá absence
 * a podobné situace).
 *
 * Odpovědnosti:
 * - evidence období, kdy se hráč nemůže účastnit zápasů,
 * - poskytování přehledů období neaktivity pro konkrétního hráče i pro administraci,
 * - ověřování, zda je hráč v daném okamžiku aktivní.
 *
 * Rozhraní:
 * - pracuje s DTO objektem PlayerInactivityPeriodDTO, nikoliv přímo s entitami,
 * - odděluje business logiku neaktivity od persistence vrstvy.
 *
 * Nerozpracovává:
 * - autorizaci a role uživatelů,
 * - notifikace,
 * - logiku uživatelského rozhraní.
 */
public interface PlayerInactivityPeriodService {

    /**
     * Vrátí seznam všech období neaktivity v systému.
     *
     * Metoda se typicky používá v administrátorských přehledech
     * nebo hromadných exportech.
     *
     * @return seznam všech období neaktivity ve formě DTO
     */
    List<PlayerInactivityPeriodDTO> getAll();

    /**
     * Vrátí období neaktivity podle jeho identifikátoru.
     *
     * @param id identifikátor období neaktivity
     * @return období neaktivity ve formě DTO
     */
    PlayerInactivityPeriodDTO getById(Long id);

    /**
     * Vrátí seznam období neaktivity pro konkrétního hráče.
     *
     * @param playerId identifikátor hráče
     * @return seznam období neaktivity daného hráče
     */
    List<PlayerInactivityPeriodDTO> getByPlayer(Long playerId);

    /**
     * Vytvoří nové období neaktivity hráče.
     *
     * Implementace je odpovědná za:
     * - validaci časového rozsahu (začátek je před koncem),
     * - kontrolu překryvů s existujícími obdobími neaktivity hráče.
     *
     * @param dto data nového období neaktivity
     * @return vytvořené období neaktivity ve formě DTO
     */
    PlayerInactivityPeriodDTO create(PlayerInactivityPeriodDTO dto);

    /**
     * Aktualizuje existující období neaktivity.
     *
     * Implementace je odpovědná za:
     * - validaci časového rozsahu,
     * - kontrolu překryvů s ostatními obdobími neaktivity daného hráče.
     *
     * @param id identifikátor období neaktivity, které má být upraveno
     * @param dto nové hodnoty období neaktivity
     * @return aktualizované období neaktivity ve formě DTO
     */
    PlayerInactivityPeriodDTO update(Long id, PlayerInactivityPeriodDTO dto);

    /**
     * Odstraní období neaktivity podle identifikátoru.
     *
     * Metoda trvale odstraní záznam z perzistentní vrstvy.
     * Případné kontroly závislostí jsou ponechány na implementaci.
     *
     * @param id identifikátor období neaktivity, které má být smazáno
     */
    void delete(Long id);

    /**
     * Ověří, zda je hráč v daném okamžiku aktivní.
     *
     * Metoda vrací informaci, zda se zadaný čas nenachází
     * v žádném z evidovaných období neaktivity hráče.
     *
     * Typické použití:
     * - při registraci hráče na zápas,
     * - při validaci účasti hráče v konkrétním čase,
     * - při filtrování dostupných zápasů pro hráče.
     *
     * @param player hráč, jehož aktivita se ověřuje
     * @param dateTime časový okamžik, pro který se aktivita kontroluje
     * @return true, pokud je hráč v daném čase aktivní, jinak false
     */
    boolean isActive(PlayerEntity player, LocalDateTime dateTime);
}