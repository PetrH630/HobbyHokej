package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repozitář pro práci s entitou MatchRegistrationEntity.
 *
 * Slouží k načítání a správě registrací hráčů k zápasům,
 * včetně kontroly existence registrace, počtů hráčů
 * a vyhledávání podle zápasu nebo hráče.
 */
@Repository
public interface MatchRegistrationRepository
        extends JpaRepository<MatchRegistrationEntity, Long> {

    /**
     * Ověří, zda existuje registrace hráče k danému zápasu.
     *
     * Používá se zejména k rychlé kontrole existence
     * registrace bez nutnosti načítat celou entitu.
     *
     * @param playerId ID hráče
     * @param matchId  ID zápasu
     * @return true, pokud registrace existuje
     */
    Boolean existsByPlayerIdAndMatchId(Long playerId, Long matchId);

    /**
     * Vrátí všechny registrace k danému zápasu.
     *
     * @param matchId ID zápasu
     * @return seznam registrací
     */
    List<MatchRegistrationEntity> findByMatchId(Long matchId);

    /**
     * Vrátí všechny registrace daného hráče.
     *
     * @param playerId ID hráče
     * @return seznam registrací
     */
    List<MatchRegistrationEntity> findByPlayerId(Long playerId);

    /**
     * Najde konkrétní registraci hráče k zápasu.
     *
     * @param playerId ID hráče
     * @param matchId  ID zápasu
     * @return registrace zabalená v Optional, pokud existuje
     */
    Optional<MatchRegistrationEntity> findByPlayerIdAndMatchId(Long playerId, Long matchId);

    /**
     * Spočítá počet registrací daného zápasu podle stavu.
     *
     * Typicky se používá pro zjištění aktuální
     * obsazenosti zápasu.
     *
     * @param matchId ID zápasu
     * @param status  stav registrace
     * @return počet registrací v daném stavu
     */
    long countByMatchIdAndStatus(Long matchId, PlayerMatchStatus status);

    /**
     * Vrátí registrace pro více zápasů najednou.
     *
     * Používá se například při hromadném načítání
     * registrací pro přehledy a statistiky.
     *
     * @param matchIds seznam ID zápasů
     * @return seznam registrací
     */
    List<MatchRegistrationEntity> findByMatchIdIn(List<Long> matchIds);
}
