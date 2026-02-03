package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.MatchRegistrationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repozitář pro práci s entitou MatchRegistrationHistoryEntity.
 *
 * Slouží k načítání historických záznamů změn
 * registrací hráčů k zápasům.
 */
public interface MatchRegistrationHistoryRepository
        extends JpaRepository<MatchRegistrationHistoryEntity, Long> {

    /**
     * Vrátí historii změn konkrétní registrace.
     *
     * Záznamy jsou seřazeny sestupně podle času změny,
     * takže nejnovější změna je na prvním místě.
     *
     * @param matchRegistrationId ID původní registrace
     * @return seznam historických záznamů
     */
    List<MatchRegistrationHistoryEntity>
    findByMatchRegistrationIdOrderByChangedAtDesc(Long matchRegistrationId);

    /**
     * Vrátí historii všech změn registrací pro daný zápas.
     *
     * Používá se například pro administrativní přehledy
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

    /**
     * Vrátí historii všech změn registrací konkrétního hráče
     * v konkrétním zápase.
     *
     * @param matchId  ID zápasu
     * @param playerId ID hráče
     * @return seznam historických záznamů
     */
    List<MatchRegistrationHistoryEntity>
    findByMatchIdAndPlayerIdOrderByChangedAtDesc(Long matchId, Long playerId);
}
