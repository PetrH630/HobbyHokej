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
     *
     * @param userId ID uživatele z hlavní tabulky app_users
     * @return seznam historických záznamů daného uživatele
     */
    List<AppUserHistoryEntity> findByUserIdOrderByChangedAtDesc(Long userId);

    /**
     * Vrátí všechny historické záznamy pro daný e-mail,
     * seřazené od nejnovější změny po nejstarší.
     *
     * Umožňuje dohledat historii změn i v případě, kdy
     * není k dispozici ID uživatele nebo se změnila vazba
     * mezi e-mailem a účtem.
     *
     * @param email e-mail uživatele
     * @return seznam historických záznamů pro daný e-mail
     */
    List<AppUserHistoryEntity> findByEmailOrderByChangedAtDesc(String email);

}