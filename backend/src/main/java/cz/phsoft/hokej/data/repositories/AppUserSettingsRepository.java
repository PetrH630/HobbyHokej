package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.AppUserSettingsEntity;
import cz.phsoft.hokej.data.entities.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repozitář pro práci s nastavením uživatele (AppUserSettingsEntity).
 */
public interface AppUserSettingsRepository extends JpaRepository<AppUserSettingsEntity, Long> {

    Optional<AppUserSettingsEntity> findByUser(AppUserEntity user);

    Optional<AppUserSettingsEntity> findByUserEmail(String email);

    Boolean existsByUser(AppUserEntity user);
}
