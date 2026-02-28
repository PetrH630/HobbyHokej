package cz.phsoft.hokej.player.repositories;

import cz.phsoft.hokej.player.entities.PlayerHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repozitář pro čtení historických záznamů hráčů.
 *
 * Slouží pro auditní a přehledové účely.
 * Zápis do historie zajišťují databázové triggery.
 */
public interface PlayerHistoryRepository
        extends JpaRepository<PlayerHistoryEntity, Long> {

    /**
     * Vrátí všechny historické záznamy pro daného hráče,
     * seřazené od nejnovější změny po nejstarší.
     */
    List<PlayerHistoryEntity>
    findByPlayerIdOrderByChangedAtDesc(Long playerId);
}