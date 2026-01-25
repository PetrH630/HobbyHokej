package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.SeasonEntity;
import cz.phsoft.hokej.models.dto.SeasonDTO;

import java.util.List;

/**
 * Rozhraní pro správu sezón v aplikaci.
 * <p>
 * Definuje kontrakt pro práci se sezónami, které slouží
 * jako časový rámec pro organizaci zápasů, statistik
 * a dalších herních dat.
 * </p>
 *
 * Účel:
 * <ul>
 *     <li>správa životního cyklu sezón (vytvoření, úprava, aktivace),</li>
 *     <li>určení aktuálně aktivní sezóny,</li>
 *     <li>poskytnutí přehledu všech sezón v systému.</li>
 * </ul>
 *
 * Použití:
 * <ul>
 *     <li>využívá se v controllerech a business službách,</li>
 *     <li>slouží jako centrální zdroj informace o aktivní sezóně.</li>
 * </ul>
 *
 * Architektonické zásady:
 * <ul>
 *     <li>většina operací pracuje s DTO objekty,</li>
 *     <li>získání aktivní sezóny může vracet entitu
 *     (např. pro interní business logiku).</li>
 * </ul>
 */
public interface SeasonService {

    /**
     * Vytvoří novou sezónu.
     * <p>
     * Implementace je zodpovědná za validaci období sezóny
     * (např. že začátek je před koncem a že se sezóny nepřekrývají).
     * </p>
     *
     * @param season data nové sezóny
     * @return vytvořená sezóna
     */
    SeasonDTO createSeason(SeasonDTO season);

    /**
     * Aktualizuje existující sezónu.
     *
     * @param id     ID sezóny, která má být aktualizována
     * @param season nové hodnoty sezóny
     * @return aktualizovaná sezóna
     */
    SeasonDTO updateSeason(Long id, SeasonDTO season);

    /**
     * Vrátí aktuálně aktivní sezónu.
     * <p>
     * Aktivní sezóna představuje časový rámec,
     * ve kterém jsou zápasy považovány za platné.
     * </p>
     *
     * @return aktivní sezóna jako entita
     */
    SeasonEntity getActiveSeason();

    /**
     * Vrátí seznam všech sezón v systému.
     * <p>
     * Typicky slouží pro administrátorské přehledy.
     * </p>
     *
     * @return seznam všech sezón
     */
    List<SeasonDTO> getAllSeasons();

    /**
     * Nastaví zadanou sezónu jako aktivní.
     * <p>
     * Implementace zajistí, že v systému existuje
     * vždy maximálně jedna aktivní sezóna.
     * </p>
     *
     * @param seasonId ID sezóny, která má být nastavena jako aktivní
     */
    void setActiveSeason(Long seasonId);
}
