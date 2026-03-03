package cz.phsoft.hokej.match.repositories;

import cz.phsoft.hokej.match.entities.MatchHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA repozitář pro práci s entitou MatchHistoryEntity.
 *
 * Slouží výhradně pro čtení historických záznamů zápasů
 * pro auditní a administrativní účely.
 *
 * Zápis do historické tabulky je zajišťován databázovými
 * triggery nebo jiným infrastrukturním mechanismem,
 * nikoli přímo aplikační logikou tohoto repozitáře.
 */
public interface MatchHistoryRepository extends JpaRepository<MatchHistoryEntity, Long> {

    /**
     * Vrátí všechny historické záznamy pro daný zápas,
     * seřazené od nejnovější změny po nejstarší.
     *
     * @param matchId identifikátor zápasu
     * @return seznam historických záznamů
     */
    List<MatchHistoryEntity> findByMatchIdOrderByChangedAtDesc(Long matchId);
}