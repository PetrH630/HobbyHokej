package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.SeasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repozitář pro práci s entitou {@link SeasonEntity}.
 *
 * Slouží ke správě sezón, zejména k určení aktivní sezóny,
 * kontrole časových překryvů a načítání sezón
 * v chronologickém pořadí.
 */
public interface SeasonRepository extends JpaRepository<SeasonEntity, Long> {

    /**
     * Vrátí aktuálně aktivní sezónu.
     *
     * V systému může být v daném okamžiku
     * aktivní maximálně jedna sezóna.
     *
     * @return aktivní sezóna zabalená v {@link Optional}, pokud existuje
     */
    Optional<SeasonEntity> findByActiveTrue();

    /**
     * Vrátí všechny sezóny seřazené
     * podle data začátku vzestupně.
     *
     * @return seznam sezón
     */
    List<SeasonEntity> findAllByOrderByStartDateAsc();

    /**
     * Ověří, zda existuje sezóna, která se časově
     * překrývá se zadaným intervalem.
     *
     * Používá se při vytváření nové sezóny
     * jako ochrana proti překrývajícím se obdobím.
     *
     * @param endDate   konec kontrolovaného intervalu
     * @param startDate začátek kontrolovaného intervalu
     * @return {@code true}, pokud existuje časový překryv
     */
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate endDate,
            LocalDate startDate
    );

    /**
     * Ověří, zda existuje jiná sezóna (mimo zadané ID),
     * která se časově překrývá se zadaným intervalem.
     *
     * Používá se při aktualizaci existující sezóny,
     * aby nedošlo ke kolizi s jinou sezónou.
     *
     * @param endDate   konec kontrolovaného intervalu
     * @param startDate začátek kontrolovaného intervalu
     * @param id        ID sezóny, která má být z kontroly vynechána
     * @return {@code true}, pokud existuje časový překryv
     */
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
            LocalDate endDate,
            LocalDate startDate,
            Long id
    );

    /**
     * Spočítá počet aktivních sezón.
     *
     * Slouží jako ochrana proti stavu,
     * kdy by bylo aktivních více sezón současně.
     *
     * @return počet aktivních sezón
     */
    long countByActiveTrue();
}
