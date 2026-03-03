package cz.phsoft.hokej.match.repositories;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.match.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA repozitář pro práci s entitou MatchEntity.
 *
 * Poskytuje metody pro načítání zápasů podle času konání,
 * sezóny a jejich kombinace. Používá se zejména ve službách
 * zajišťujících přehled nadcházejících a odehraných zápasů.
 */
public interface MatchRepository extends JpaRepository<MatchEntity, Long> {

    // Zápasy podle data nezávisle na sezóně

    /**
     * Vrátí všechny zápasy konající se po zadaném čase.
     *
     * Výsledek je seřazen vzestupně podle data a času konání,
     * nejbližší zápas je na prvním místě.
     *
     * @param dateTime referenční datum a čas
     * @return seznam nadcházejících zápasů
     */
    List<MatchEntity> findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime dateTime);

    /**
     * Vrátí všechny zápasy konající se před zadaným časem.
     *
     * Výsledek je seřazen sestupně podle data a času konání,
     * nejnovější odehraný zápas je na prvním místě.
     *
     * @param dateTime referenční datum a čas
     * @return seznam odehraných zápasů
     */
    List<MatchEntity> findByDateTimeBeforeOrderByDateTimeDesc(LocalDateTime dateTime);

    // Zápasy v konkrétní sezóně

    /**
     * Vrátí všechny zápasy v dané sezóně seřazené
     * vzestupně podle data a času konání.
     *
     * @param seasonId identifikátor sezóny
     * @return seznam zápasů v sezóně
     */
    List<MatchEntity> findAllBySeasonIdOrderByDateTimeAsc(Long seasonId);

    /**
     * Vrátí všechny zápasy v dané sezóně seřazené
     * sestupně podle data a času konání.
     *
     * @param seasonId identifikátor sezóny
     * @return seznam zápasů v sezóně
     */
    List<MatchEntity> findAllBySeasonIdOrderByDateTimeDesc(Long seasonId);

    // Zápasy v sezóně s časovým omezením

    /**
     * Vrátí zápasy v dané sezóně, které se konají
     * po zadaném čase.
     *
     * Používá se zejména pro načítání nadcházejících
     * zápasů v aktivní sezóně.
     *
     * @param seasonId identifikátor sezóny
     * @param from referenční datum a čas
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
     * @param seasonId identifikátor sezóny
     * @param to referenční datum a čas
     * @return seznam odehraných zápasů v sezóně
     */
    List<MatchEntity> findBySeasonIdAndDateTimeBeforeOrderByDateTimeDesc(
            Long seasonId,
            LocalDateTime to
    );

    /**
     * Vrátí všechny zápasy v daném časovém intervalu.
     *
     * @param from počáteční datum a čas intervalu
     * @param to koncové datum a čas intervalu
     * @return seznam zápasů v daném intervalu
     */
    List<MatchEntity> findByDateTimeBetween(LocalDateTime from, LocalDateTime to);

    /**
     * Vrátí zápasy v daném časovém intervalu,
     * které nemají zadaný konkrétní stav.
     *
     * Používá se například pro filtrování
     * aktivních nebo nezrušených zápasů.
     *
     * @param limitFrom počáteční datum a čas intervalu
     * @param limitTo koncové datum a čas intervalu
     * @param matchStatus stav, který má být vyloučen
     * @return seznam odpovídajících zápasů
     */
    List<MatchEntity> findByDateTimeBetweenAndMatchStatusNot(
            LocalDateTime limitFrom,
            LocalDateTime limitTo,
            MatchStatus matchStatus
    );
}