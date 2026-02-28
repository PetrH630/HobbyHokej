package cz.phsoft.hokej.user.repositories;

import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.user.entities.EmailVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repozitář pro práci s entitou EmailVerificationTokenEntity.
 *
 * Slouží k ukládání a vyhledávání ověřovacích tokenů
 * používaných při aktivaci uživatelských účtů.
 */
public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationTokenEntity, Long> {

    /**
     * Vyhledá ověřovací token podle jeho hodnoty.
     *
     * Používá se při ověřování aktivačního odkazu uživatele.
     *
     * @param token hodnota ověřovacího tokenu
     * @return token zabalený v Optional, pokud existuje
     */
    Optional<EmailVerificationTokenEntity> findByToken(String token);

    /**
     * Smaže všechny ověřovací tokeny daného uživatele.
     *
     * Typicky se používá po úspěšné aktivaci účtu
     * nebo při opakovaném posílání aktivačního e-mailu.
     *
     * @param user uživatel, jehož tokeny mají být smazány
     */
    void deleteByUser(AppUserEntity user);
}
