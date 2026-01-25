package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repozitář pro práci s entitou {@link AppUserEntity}.
 *
 * Slouží k perzistenci a načítání uživatelských účtů
 * z databáze pomocí Spring Data JPA.
 */
public interface AppUserRepository extends JpaRepository<AppUserEntity, Long> {

    /**
     * Vyhledá uživatele podle emailové adresy.
     *
     * Email slouží jako unikátní identifikátor uživatele
     * v rámci autentizace.
     *
     * @param email email uživatele
     * @return uživatel zabalený v {@link Optional}, pokud existuje
     */
    Optional<AppUserEntity> findByEmail(String email);
}
