package cz.phsoft.hokej.registration.services;

import cz.phsoft.hokej.registration.enums.ExcuseReason;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.registration.dto.MatchRegistrationRequest;

/**
 * Service vrstva pro příkazové operace nad registracemi hráčů na zápasy.
 *
 * Rozhraní se používá pro změny stavu registrací, změny týmu a pozic,
 * přepočet kapacity při změně počtu hráčů a pro hromadné odesílání SMS.
 *
 * Neřeší čtecí operace nad registracemi (ty zůstávají v {@link MatchRegistrationService}).
 */
public interface MatchRegistrationCommandService {

    /**
     * Vytváří nebo aktualizuje registraci hráče na zápas.
     *
     * @param playerId Identifikátor hráče.
     * @param request  Požadavek na změnu registrace.
     * @return Uložená registrace převedená do DTO.
     */
    MatchRegistrationDTO upsertRegistration(Long playerId, MatchRegistrationRequest request);

    /**
     * Nastavuje registraci hráče do stavu NO_EXCUSED.
     *
     * @param matchId   Identifikátor zápasu.
     * @param playerId  Identifikátor hráče.
     * @param adminNote Poznámka administrátora.
     * @return Aktualizovaná registrace převedená do DTO.
     */
    MatchRegistrationDTO markNoExcused(Long matchId, Long playerId, String adminNote);

    /**
     * Ruší stav NO_EXCUSED a nastavuje registraci do stavu EXCUSED.
     *
     * @param matchId      Identifikátor zápasu.
     * @param playerId     Identifikátor hráče.
     * @param excuseReason Důvod omluvy.
     * @param excuseNote   Poznámka omluvy.
     * @return Aktualizovaná registrace převedená do DTO.
     */
    MatchRegistrationDTO cancelNoExcused(Long matchId,
                                         Long playerId,
                                         ExcuseReason excuseReason,
                                         String excuseNote);

    /**
     * Mění tým hráče v rámci registrace na zápas.
     *
     * @param playerId Identifikátor hráče.
     * @param matchId  Identifikátor zápasu.
     * @return Aktualizovaná registrace převedená do DTO.
     */
    MatchRegistrationDTO changeRegistrationTeam(Long playerId, Long matchId);

    /**
     * Mění pozici hráče v rámci konkrétního zápasu.
     *
     * @param playerId        Identifikátor hráče.
     * @param matchId         Identifikátor zápasu.
     * @param positionInMatch Cílová pozice v zápase.
     * @return Aktualizovaná registrace převedená do DTO.
     */
    MatchRegistrationDTO changeRegistrationPosition(Long playerId,
                                                    Long matchId,
                                                    PlayerPosition positionInMatch);

    /**
     * Provádí administrátorskou změnu stavu registrace.
     *
     * @param matchId  Identifikátor zápasu.
     * @param playerId Identifikátor hráče.
     * @param status   Cílový stav registrace.
     * @return Aktualizovaná registrace převedená do DTO.
     */
    MatchRegistrationDTO updateStatus(Long matchId,
                                      Long playerId,
                                      PlayerMatchStatus status);

    /**
     * Přepočítává stavy REGISTERED a RESERVED podle aktuální kapacity zápasu.
     *
     * @param matchId Identifikátor zápasu.
     */
    void recalcStatusesForMatch(Long matchId);

    /**
     * Povyšuje kandidáty ze stavu RESERVED do stavu REGISTERED
     * při navýšení kapacity zápasu.
     *
     * @param matchId       Identifikátor zápasu.
     * @param freedTeam     Tým, ve kterém se místo uvolnilo (nebo preferovaný tým).
     * @param freedPosition Pozice, která se uvolnila (nebo null).
     * @param slotsCount    Počet nových míst, která se mají obsadit.
     */
    void promoteReservedCandidatesForCapacityIncrease(Long matchId,
                                                      Team freedTeam,
                                                      PlayerPosition freedPosition,
                                                      int slotsCount);

    /**
     * Odesílá SMS zprávu všem hráčům ve stavu REGISTERED,
     * kteří mají povolené SMS notifikace.
     *
     * @param matchId Identifikátor zápasu.
     */
    void sendSmsToRegisteredPlayers(Long matchId);
}