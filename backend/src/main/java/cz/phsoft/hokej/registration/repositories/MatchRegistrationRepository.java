package cz.phsoft.hokej.registration.repositories;

import cz.phsoft.hokej.registration.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
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
     * Ověří existenci registrace hráče k danému zápasu.
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
     * @return Optional obsahující registraci, pokud existuje
     */
    Optional<MatchRegistrationEntity> findByPlayerIdAndMatchId(Long playerId, Long matchId);

    /**
     * Spočítá počet registrací daného zápasu podle stavu.
     *
     * @param matchId ID zápasu
     * @param status  stav registrace
     * @return počet registrací v daném stavu
     */
    long countByMatchIdAndStatus(Long matchId, PlayerMatchStatus status);

    /**
     * Vrátí registrace pro více zápasů najednou.
     *
     * @param matchIds seznam ID zápasů
     * @return seznam registrací
     */
    List<MatchRegistrationEntity> findByMatchIdIn(List<Long> matchIds);

    /**
     * Najde všechny registrace pro daný zápas a daný stav.
     *
     * @param matchId ID zápasu
     * @param status  stav registrace hráče
     * @return seznam registrací
     */
    List<MatchRegistrationEntity> findByMatchIdAndStatus(Long matchId, PlayerMatchStatus status);

    /**
     * Najde registrace pro daný zápas a stav,
     * u kterých ještě nebyla odeslána připomínka.
     *
     * @param matchId ID zápasu
     * @param status  stav registrace hráče
     * @return seznam registrací bez odeslané připomínky
     */
    List<MatchRegistrationEntity> findByMatchIdAndStatusAndReminderAlreadySentFalse(
            Long matchId,
            PlayerMatchStatus status
    );

    /**
     * Vrátí registrace pro daný zápas a stav
     * seřazené podle času vytvoření vzestupně.
     *
     * @param matchId ID zápasu
     * @param status  stav registrace hráče
     * @return seřazený seznam registrací
     */
    List<MatchRegistrationEntity> findByMatchIdAndStatusOrderByTimestampAsc(
            Long matchId,
            PlayerMatchStatus status
    );

    List<MatchRegistrationEntity> findByMatchIdAndStatusIn(
            Long matchId,
            List<PlayerMatchStatus> statuses
    );

}