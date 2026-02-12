package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.SeasonHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repozitář pro práci s entitou SeasonHistoryEntity.
 *
 * Slouží k přístupu k historickým záznamům sezóny uloženým v databázi.
 * Poskytuje základní CRUD operace zděděné z JpaRepository a
 * specifické dotazy používané servisní vrstvou pro načítání historie
 * konkrétní sezóny.
 *
 * Repozitář neobsahuje business logiku. Je používán servisní vrstvou,
 * která zajišťuje aplikační pravidla a případné transformace dat.
 */
public interface SeasonHistoryRepository
        extends JpaRepository<SeasonHistoryEntity, Long> {

    /**
     * Vrátí historii změn konkrétní sezóny.
     *
     * Záznamy jsou seřazeny sestupně podle času změny, takže
     * nejnovější změna je na prvním místě.
     *
     * @param seasonId identifikátor sezóny, pro kterou se má historie načíst
     * @return seznam historických záznamů dané sezóny seřazený od nejnovějšího
     */
    List<SeasonHistoryEntity> findBySeasonIdOrderByChangedAtDesc(Long seasonId);
}
