package cz.phsoft.hokej.registration.repositories;

import cz.phsoft.hokej.registration.entities.MatchRegistrationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repozitář pro práci s entitou MatchRegistrationHistoryEntity.
 *
 * Slouží k načítání historických záznamů změn
 * registrací hráčů k zápasům pro auditní a přehledové účely.
 */
public interface MatchRegistrationHistoryRepository
        extends JpaRepository<MatchRegistrationHistoryEntity, Long> {

    /**
     * Vrátí historii změn konkrétní registrace.
     *
     * @param matchRegistrationId ID původní registrace
     * @return seznam historických záznamů seřazených sestupně podle času změny
     */
    List<MatchRegistrationHistoryEntity>
    findByMatchRegistrationIdOrderByChangedAtDesc(Long matchRegistrationId);

    /**
     * Vrátí historii všech změn registrací pro daný zápas.
     *
     * @param matchId ID zápasu
     * @return seznam historických záznamů
     */
    List<MatchRegistrationHistoryEntity>
    findByMatchIdOrderByChangedAtDesc(Long matchId);

    /**
     * Vrátí historii změn registrací konkrétního hráče.
     *
     * @param playerId ID hráče
     * @return seznam historických záznamů
     */
    List<MatchRegistrationHistoryEntity>
    findByPlayerIdOrderByChangedAtDesc(Long playerId);

    /**
     * Vrátí historii změn registrací konkrétního hráče
     * v konkrétním zápase.
     *
     * @param matchId  ID zápasu
     * @param playerId ID hráče
     * @return seznam historických záznamů
     */
    List<MatchRegistrationHistoryEntity>
    findByMatchIdAndPlayerIdOrderByChangedAtDesc(Long matchId, Long playerId);
}