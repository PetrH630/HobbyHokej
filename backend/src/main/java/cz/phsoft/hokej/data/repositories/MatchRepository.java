package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repozitář pro práci s entitou {@link MatchEntity}.
 *
 * Slouží k načítání zápasů podle času konání a sezóny,
 * zejména pro přehledy nadcházejících a odehraných zápasů.
 */
public interface MatchRepository extends JpaRepository<MatchEntity, Long> {

    // ==================================================
    // ZÁPASY PODLE DATA (NEZÁVISLE NA SEZÓNĚ)
    // ==================================================

    /**
     * Vrátí všechny zápasy konající se po zadaném čase.
     *
     * Zápasy jsou seřazeny vzestupně podle data konání
     * (nejbližší zápas jako první).
     *
     * @param dateTime referenční datum a čas
     * @return seznam nadcházejících zápasů
     */
    List<MatchEntity> findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime dateTime);

    /**
     * Vrátí všechny zápasy konající se před zadaným časem.
     *
     * Zápasy jsou seřazeny sestupně podle data konání
     * (nejnovější odehraný zápas jako první).
     *
     * @param dateTime referenční datum a čas
     * @return seznam odehraných zápasů
     */
    List<MatchEntity> findByDateTimeBeforeOrderByDateTimeDesc(LocalDateTime dateTime);

    // ==================================================
    // ZÁPASY V KONKRÉTNÍ SEZÓNĚ
    // ==================================================

    /**
     * Vrátí všechny zápasy v dané sezóně seřazené
     * vzestupně podle data konání.
     *
     * @param seasonId ID sezóny
     * @return seznam zápasů v sezóně
     */
    List<MatchEntity> findAllBySeasonIdOrderByDateTimeAsc(Long seasonId);

    /**
     * Vrátí všechny zápasy v dané sezóně seřazené
     * sestupně podle data konání.
     *
     * @param seasonId ID sezóny
     * @return seznam zápasů v sezóně
     */
    List<MatchEntity> findAllBySeasonIdOrderByDateTimeDesc(Long seasonId);

    // ==================================================
    // ZÁPASY V SEZÓNĚ S ČASOVÝM OMEZENÍM
    // ==================================================

    /**
     * Vrátí zápasy v dané sezóně, které se konají
     * po zadaném čase.
     *
     * Používá se zejména pro načítání nadcházejících
     * zápasů v aktivní sezóně.
     *
     * @param seasonId ID sezóny
     * @param from     referenční datum a čas
     * @return seznam nadcházejících zápasů v sezóně
     */
    List<MatchEntity> findBySeasonIdAndDateTimeAfterOrderByDateTimeAsc(
            Long seasonId,
            LocalDateTime from
    );

    /**
     * Vrátí zápasy v dané sezóně, které se konaly
     * před zadaným časem.
     *
     * Používá se zejména pro přehled odehraných
     * zápasů v sezóně.
     *
     * @param seasonId ID sezóny
     * @param to       referenční datum a čas
     * @return seznam odehraných zápasů v sezóně
     */
    List<MatchEntity> findBySeasonIdAndDateTimeBeforeOrderByDateTimeDesc(
            Long seasonId,
            LocalDateTime to
    );
}
