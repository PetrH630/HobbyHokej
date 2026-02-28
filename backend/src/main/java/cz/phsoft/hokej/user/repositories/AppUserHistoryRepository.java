package cz.phsoft.hokej.user.repositories;

import cz.phsoft.hokej.user.entities.AppUserHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repozitář pro čtení historických záznamů uživatelů.
 *
 * Slouží pro auditní a přehledové účely. Zápis do historie
 * zajišťují databázové triggery, nikoliv tento repozitář.
 */
public interface AppUserHistoryRepository extends JpaRepository<AppUserHistoryEntity, Long> {

    /**
     * Vrátí všechny historické záznamy pro daného uživatele,
     * seřazené od nejnovější změny po nejstarší.
     */
    List<AppUserHistoryEntity> findByUserIdOrderByChangedAtDesc(Long userId);

    List<AppUserHistoryEntity> findByEmailOrderByChangedAtDesc(String email);


}
