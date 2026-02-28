package cz.phsoft.hokej.season.repositories;

import cz.phsoft.hokej.season.entities.SeasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repozitář pro práci s entitou SeasonEntity.
 *
 * Slouží k přístupu k datům sezón uloženým v databázi.
 * Poskytuje základní CRUD operace zděděné z JpaRepository
 * a specifické dotazy používané servisní vrstvou pro kontrolu
 * jedinečnosti názvu, aktivní sezóny a časových překryvů.
 *
 * Repozitář neobsahuje business logiku. Ověřování aplikačních pravidel
 * a rozhodování o dalším postupu je prováděno v servisní vrstvě.
 */
public interface SeasonRepository extends JpaRepository<SeasonEntity, Long> {

    /**
     * Ověří, zda existuje sezóna se zadaným názvem.
     *
     * Používá se při vytváření nové sezóny pro kontrolu
     * jedinečnosti názvu.
     *
     * @param name název sezóny
     * @return true, pokud sezóna se zadaným názvem existuje
     */
    boolean existsByName(String name);

    /**
     * Ověří, zda existuje jiná sezóna se zadaným názvem
     * mimo zadané ID.
     *
     * Používá se při aktualizaci sezóny pro kontrolu,
     * aby nedošlo ke kolizi názvů.
     *
     * @param name název sezóny
     * @param id identifikátor sezóny, která má být z kontroly vynechána
     * @return true, pokud existuje jiná sezóna se stejným názvem
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Vrátí aktuálně aktivní sezónu.
     *
     * V systému může být v jednom okamžiku aktivní
     * maximálně jedna sezóna. Ověření tohoto pravidla
     * je prováděno v servisní vrstvě.
     *
     * @return aktivní sezóna zabalená v Optional, pokud existuje
     */
    Optional<SeasonEntity> findByActiveTrue();

    /**
     * Vrátí všechny sezóny seřazené podle data začátku vzestupně.
     *
     * Používá se pro přehledové výpisy sezón
     * v chronologickém pořadí.
     *
     * @return seznam sezón seřazený podle data začátku
     */
    List<SeasonEntity> findAllByOrderByStartDateAsc();

    /**
     * Ověří, zda existuje sezóna, která se časově překrývá
     * se zadaným intervalem.
     *
     * Používá se při vytváření nové sezóny jako ochrana
     * proti překrývajícím se časovým obdobím.
     *
     * @param endDate konec kontrolovaného intervalu
     * @param startDate začátek kontrolovaného intervalu
     * @return true, pokud existuje časový překryv
     */
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate endDate,
            LocalDate startDate
    );

    /**
     * Ověří, zda existuje jiná sezóna mimo zadané ID,
     * která se časově překrývá se zadaným intervalem.
     *
     * Používá se při aktualizaci existující sezóny,
     * aby nedošlo ke kolizi s jiným obdobím.
     *
     * @param endDate konec kontrolovaného intervalu
     * @param startDate začátek kontrolovaného intervalu
     * @param id identifikátor sezóny, která má být z kontroly vynechána
     * @return true, pokud existuje časový překryv
     */
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
            LocalDate endDate,
            LocalDate startDate,
            Long id
    );

    /**
     * Spočítá počet aktivních sezón.
     *
     * Používá se jako ochrana proti stavu,
     * kdy by bylo aktivních více sezón současně.
     * Kontrola konzistence je prováděna v servisní vrstvě.
     *
     * @return počet aktivních sezón
     */
    long countByActiveTrue();
}
