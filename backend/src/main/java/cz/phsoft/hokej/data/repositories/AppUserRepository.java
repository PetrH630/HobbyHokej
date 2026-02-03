package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repozitář pro práci s entitou AppUserEntity.
 *
 * Slouží k perzistenci a načítání uživatelských účtů
 * z databáze pomocí Spring Data JPA.
 */
public interface AppUserRepository extends JpaRepository<AppUserEntity, Long> {

    /**
     * Vyhledá uživatele podle e-mailové adresy.
     *
     * E-mail slouží jako unikátní identifikátor uživatele
     * při přihlášení do aplikace.
     *
     * @param email e-mail uživatele
     * @return uživatel zabalený v Optional, pokud existuje
     */
    Optional<AppUserEntity> findByEmail(String email);
}
