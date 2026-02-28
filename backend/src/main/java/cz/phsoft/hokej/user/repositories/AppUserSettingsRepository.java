package cz.phsoft.hokej.user.repositories;

import cz.phsoft.hokej.user.entities.AppUserSettingsEntity;
import cz.phsoft.hokej.user.entities.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repozitář pro práci s entitou AppUserSettingsEntity.
 *
 * Slouží k načítání a ukládání nastavení uživatelských účtů,
 * oddělených od samotných uživatelů.
 */
public interface AppUserSettingsRepository extends JpaRepository<AppUserSettingsEntity, Long> {

    /**
     * Vyhledá nastavení podle uživatele.
     *
     * @param user uživatelský účet
     * @return nastavení zabalené v Optional, pokud existuje
     */
    Optional<AppUserSettingsEntity> findByUser(AppUserEntity user);

    /**
     * Vyhledá nastavení podle e-mailu uživatele.
     *
     * @param email e-mail uživatele
     * @return nastavení zabalené v Optional, pokud existuje
     */
    Optional<AppUserSettingsEntity> findByUserEmail(String email);

    /**
     * Ověří, zda existuje záznam nastavení pro daného uživatele.
     *
     * @param user uživatelský účet
     * @return true, pokud nastavení existuje
     */
    Boolean existsByUser(AppUserEntity user);
}
