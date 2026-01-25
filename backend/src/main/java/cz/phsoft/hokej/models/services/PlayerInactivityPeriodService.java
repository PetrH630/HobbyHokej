package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.models.dto.PlayerInactivityPeriodDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Rozhraní pro správu období neaktivity hráčů.
 * <p>
 * Definuje kontrakt pro práci s časovými úseky, ve kterých
 * je hráč považován za neaktivního (např. zranění, dovolená,
 * dlouhodobá absence).
 * </p>
 *
 * Účel:
 * <ul>
 *     <li>evidence období, kdy se hráč nemůže účastnit zápasů,</li>
 *     <li>poskytnutí přehledů neaktivity pro hráče i administraci,</li>
 *     <li>umožnění kontroly, zda je hráč v daný okamžik aktivní.</li>
 * </ul>
 *
 * Použití:
 * <ul>
 *     <li>využívá se při registraci hráčů na zápasy,</li>
 *     <li>slouží pro validaci účasti hráče v konkrétním čase,</li>
 *     <li>je součástí business pravidel plánování zápasů.</li>
 * </ul>
 *
 * Architektonické zásady:
 * <ul>
 *     <li>pracuje výhradně s DTO objekty, nikoliv přímo s entitami,</li>
 *     <li>odděluje business logiku neaktivity od persistence vrstvy.</li>
 * </ul>
 */
public interface PlayerInactivityPeriodService {

    /**
     * Vrátí seznam všech období neaktivity v systému.
     * <p>
     * Typicky dostupné pouze pro administrátorské přehledy.
     * </p>
     *
     * @return seznam všech období neaktivity
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
     * <p>
     * Implementace je zodpovědná za validaci časového rozsahu
     * (např. začátek &lt; konec, nepřekrývání s jinými obdobími).
     * </p>
     *
     * @param dto data nového období neaktivity
     * @return vytvořené období neaktivity
     */
    PlayerInactivityPeriodDTO create(PlayerInactivityPeriodDTO dto);

    /**
     * Aktualizuje existující období neaktivity.
     *
     * @param id  ID období neaktivity, které má být upraveno
     * @param dto nové hodnoty období neaktivity
     * @return aktualizované období neaktivity
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
     * <p>
     * Metoda vrací informaci, zda se zadaný čas
     * nenachází v žádném z evidovaných období neaktivity hráče.
     * </p>
     *
     * Typické použití:
     * <ul>
     *     <li>při registraci hráče na zápas,</li>
     *     <li>při validaci účasti hráče v konkrétním čase.</li>
     * </ul>
     *
     * @param player   hráč, jehož aktivita se ověřuje
     * @param dateTime časový okamžik, pro který se aktivita kontroluje
     * @return {@code true}, pokud je hráč v daném čase aktivní,
     *         jinak {@code false}
     */
    boolean isActive(PlayerEntity player, LocalDateTime dateTime);
}
