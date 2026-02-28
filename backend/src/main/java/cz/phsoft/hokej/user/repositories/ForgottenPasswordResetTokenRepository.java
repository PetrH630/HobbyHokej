package cz.phsoft.hokej.user.repositories;

import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.user.entities.ForgottenPasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repozitář pro práci s entitou ForgottenPasswordResetTokenEntity.
 *
 * Slouží k ukládání a vyhledávání resetovacích tokenů
 * používaných při obnově zapomenutého hesla.
 */
public interface ForgottenPasswordResetTokenRepository
        extends JpaRepository<ForgottenPasswordResetTokenEntity, Long> {

    /**
     * Vyhledá resetovací token podle jeho hodnoty.
     *
     * @param token hodnota resetovacího tokenu
     * @return token zabalený v Optional, pokud existuje
     */
    Optional<ForgottenPasswordResetTokenEntity> findByToken(String token);

    /**
     * Smaže všechny resetovací tokeny daného uživatele.
     *
     * Typicky se používá po úspěšném nastavení nového hesla
     * nebo při vynulování procesu resetu.
     *
     * @param user uživatel, jehož tokeny mají být smazány
     */
    void deleteByUser(AppUserEntity user);
}
