package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repozitář pro práci s entitou PlayerEntity.
 *
 * Slouží k načítání a správě hráčů, zejména ve vztahu
 * k uživatelským účtům a identifikaci hráčů podle jména
 * nebo e-mailu vlastníka.
 */
public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {

    /**
     * Najde hráče podle jeho ID.
     *
     * Překrývá základní metodu z JpaRepository tak,
     * aby bylo možné vracet Optional.
     *
     * @param id ID hráče
     * @return hráč zabalený v Optional, pokud existuje
     */
    Optional<PlayerEntity> findById(Long id);

    /**
     * Vrátí všechny hráče, jejichž ID není obsaženo
     * v zadaném seznamu.
     *
     * Používá se například při filtrování dostupných hráčů.
     *
     * @param ids seznam ID hráčů, které mají být vyloučeny
     * @return seznam hráčů
     */
    List<PlayerEntity> findByIdNotIn(List<Long> ids);

    /**
     * Ověří, zda již existuje hráč se stejným jménem a příjmením.
     *
     * Používá se jako ochrana proti duplicitnímu
     * vytváření hráčů.
     *
     * @param name    jméno hráče
     * @param surname příjmení hráče
     * @return true, pokud hráč existuje
     */
    boolean existsByNameAndSurname(String name, String surname);

    /**
     * Najde hráče podle jména a příjmení.
     *
     * @param name    jméno hráče
     * @param surname příjmení hráče
     * @return hráč zabalený v Optional, pokud existuje
     */
    Optional<PlayerEntity> findByNameAndSurname(String name, String surname);

    /**
     * Najde jednoho hráče podle e-mailu uživatele,
     * ke kterému je hráč přiřazen.
     *
     * Používá se zejména v případech, kdy má uživatel
     * pouze jednoho hráče.
     *
     * @param email e-mail uživatele
     * @return hráč zabalený v Optional, pokud existuje
     */
    Optional<PlayerEntity> findByUserEmail(String email);

    /**
     * Vrátí všechny hráče patřící danému uživateli.
     *
     * @param email e-mail uživatele
     * @return seznam hráčů uživatele
     */
    List<PlayerEntity> findAllByUserEmail(String email);

    /**
     * Vrátí všechny hráče uživatele seřazené
     * podle ID vzestupně.
     *
     * Používá se pro konzistentní výpis hráčů
     * v uživatelském rozhraní.
     *
     * @param email e-mail uživatele
     * @return seznam hráčů uživatele
     */
    List<PlayerEntity> findByUser_EmailOrderByIdAsc(String email);
}
