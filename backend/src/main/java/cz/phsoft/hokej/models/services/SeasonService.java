package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.SeasonEntity;
import cz.phsoft.hokej.models.dto.SeasonDTO;

import java.util.List;

/**
 * Rozhraní pro správu sezón v aplikaci.
 *
 * Odpovědnosti:
 * - definovat kontrakt pro práci se sezónami jako časovým rámcem pro zápasy a statistiky,
 * - poskytovat operace pro vytvoření, úpravu a načtení sezón,
 * - určovat aktivní sezónu, která se používá v ostatních službách (např. MatchServiceImpl).
 *
 * Architektura:
 * - většina operací pracuje s {@link SeasonDTO} jako přenosovým objektem,
 * - metoda {@link #getActiveSeason()} vrací entitu {@link SeasonEntity}
 *   pro interní business logiku v jiných službách.
 */
public interface SeasonService {

    /**
     * Vytvoří novou sezónu.
     *
     * Odpovědnost implementace:
     * - validovat datumové rozmezí sezóny,
     * - zajistit, aby se období sezóny nepřekrývalo s jinými sezónami.
     *
     * @param season data nové sezóny
     * @return vytvořená sezóna ve formě {@link SeasonDTO}
     */
    SeasonDTO createSeason(SeasonDTO season);

    /**
     * Aktualizuje existující sezónu.
     *
     * Implementace typicky:
     * - ověřuje existenci sezóny,
     * - validuje datumové rozmezí a překryvy,
     * - aplikuje změny z DTO do entity.
     *
     * @param id     ID sezóny, která má být aktualizována
     * @param season nové hodnoty sezóny
     * @return aktualizovaná sezóna ve formě {@link SeasonDTO}
     */
    SeasonDTO updateSeason(Long id, SeasonDTO season);

    /**
     * Vrátí aktuálně aktivní sezónu.
     *
     * Aktivní sezóna představuje časový rámec, ve kterém se považují
     * zápasy a jejich statistiky za „platné“. Hodnota se používá
     * v dalších službách, například v {@code MatchServiceImpl}.
     *
     * @return aktivní sezóna jako entita {@link SeasonEntity}
     */
    SeasonEntity getActiveSeason();

    /**
     * Vrátí seznam všech sezón v systému.
     *
     * Metoda se typicky používá v administrátorských přehledech
     * a v UI pro správu sezón.
     *
     * @return seznam všech sezón ve formě {@link SeasonDTO}
     */
    List<SeasonDTO> getAllSeasons();

    /**
     * Nastaví zadanou sezónu jako aktivní.
     *
     * Implementace zajišťuje, že v systému existuje
     * vždy nejvýše jedna aktivní sezóna.
     *
     * @param seasonId ID sezóny, která má být nastavena jako aktivní
     */
    void setActiveSeason(Long seasonId);

    /**
     * Vrátí aktivní sezónu ve formě {@link SeasonDTO} nebo null,
     * pokud žádná aktivní sezóna není nastavena.
     *
     * Metoda se používá tam, kde je absence aktivní sezóny
     * platným a očekávaným stavem.
     *
     * @return aktivní sezóna nebo null
     */
    SeasonDTO getActiveSeasonOrNull();

    /**
     * Vrátí sezónu podle jejího ID.
     *
     * Používá se v administrátorské části a interní logice,
     * kde je potřeba pracovat s konkrétní sezónou.
     *
     * @param id ID sezóny
     * @return sezóna ve formě {@link SeasonDTO}
     */
    SeasonDTO getSeasonById(Long id);
}
