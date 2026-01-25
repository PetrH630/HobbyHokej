package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repozitář pro práci s entitou {@link PlayerEntity}.
 *
 * Slouží k načítání a správě hráčů, zejména ve vztahu
 * k uživatelským účtům a identifikaci hráčů podle jména
 * nebo emailu vlastníka.
 */
public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {

    /**
     * Najde hráče podle jeho ID.
     *
     * Překrývá základní metodu {@link JpaRepository},
     * aby bylo možné vracet {@link Optional}.
     *
     * @param id ID hráče
     * @return hráč zabalený v {@link Optional}, pokud existuje
     */
    Optional<PlayerEntity> findById(Long id);

    /**
     * Vrátí všechny hráče, jejichž ID není obsaženo
     * v zadaném seznamu.
     *
     * Používá se např. při filtrování dostupných hráčů.
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
     * @return {@code true}, pokud hráč existuje
     */
    boolean existsByNameAndSurname(String name, String surname);

    /**
     * Najde hráče podle jména a příjmení.
     *
     * @param name    jméno hráče
     * @param surname příjmení hráče
     * @return hráč zabalený v {@link Optional}, pokud existuje
     */
    Optional<PlayerEntity> findByNameAndSurname(String name, String surname);

    /**
     * Najde jednoho hráče podle emailu uživatele,
     * ke kterému je hráč přiřazen.
     *
     * Používá se zejména v případech, kdy má uživatel
     * pouze jednoho hráče.
     *
     * @param email email uživatele
     * @return hráč zabalený v {@link Optional}, pokud existuje
     */
    Optional<PlayerEntity> findByUserEmail(String email);

    /**
     * Vrátí všechny hráče patřící danému uživateli.
     *
     * @param email email uživatele
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
     * @param email email uživatele
     * @return seznam hráčů uživatele
     */
    List<PlayerEntity> findByUser_EmailOrderByIdAsc(String email);
}
