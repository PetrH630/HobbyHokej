package cz.phsoft.hokej.match.repositories;

import cz.phsoft.hokej.match.entities.MatchHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repozitář pro čtení historických záznamů zápasů.
 *
 * Slouží pro auditní a přehledové účely. Zápis do historie
 * zajišťují databázové triggery, nikoliv tento repozitář.
 */
public interface MatchHistoryRepository extends JpaRepository<MatchHistoryEntity, Long> {

    /**
     * Vrátí všechny historické záznamy pro daný zápas,
     * seřazené od nejnovější změny po nejstarší.
     */
    List<MatchHistoryEntity> findByMatchIdOrderByChangedAtDesc(Long matchId);
}
