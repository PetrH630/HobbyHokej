package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.MatchRegistrationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repozitář pro práci s entitou {@link MatchRegistrationHistoryEntity}.
 *
 * Slouží k načítání historických (auditních) záznamů změn
 * registrací hráčů k zápasům.
 */
public interface MatchRegistrationHistoryRepository
        extends JpaRepository<MatchRegistrationHistoryEntity, Long> {

    /**
     * Vrátí historii změn konkrétní registrace.
     *
     * Záznamy jsou seřazeny sestupně podle času změny
     * (nejnovější změna jako první).
     *
     * @param matchRegistrationId ID původní registrace
     * @return seznam historických záznamů
     */
    List<MatchRegistrationHistoryEntity>
    findByMatchRegistrationIdOrderByChangedAtDesc(Long matchRegistrationId);

    /**
     * Vrátí historii všech změn registrací pro daný zápas.
     *
     * Používá se např. pro administrativní přehledy
     * nebo auditní kontrolu zápasu.
     *
     * @param matchId ID zápasu
     * @return seznam historických záznamů
     */
    List<MatchRegistrationHistoryEntity>
    findByMatchIdOrderByChangedAtDesc(Long matchId);

    /**
     * Vrátí historii změn registrací konkrétního hráče.
     *
     * Záznamy jsou seřazeny sestupně podle času změny.
     *
     * @param playerId ID hráče
     * @return seznam historických záznamů
     */
    List<MatchRegistrationHistoryEntity>
    findByPlayerIdOrderByChangedAtDesc(Long playerId);
}
