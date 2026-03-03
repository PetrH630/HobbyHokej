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
 * Rozhraní se používá pro měnění stavu registrací, změny týmů a pozic hráčů,
 * přepočet kapacity zápasu a související přesuny hráčů mezi stavy
 * a pro hromadné odesílání SMS registrovaným hráčům.
 *
 * Neřeší čtecí operace nad registracemi. Ty jsou poskytovány
 * přes rozhraní MatchRegistrationQueryService nebo agregovanou službu
 * MatchRegistrationService.
 */
public interface MatchRegistrationCommandService {

    /**
     * Vytváří nebo aktualizuje registraci hráče na zápas.
     *
     * Pokud registrace pro kombinaci hráč a zápas existuje, je aktualizována
     * podle údajů v požadavku. Pokud neexistuje, je vytvořena nová registrace.
     * Obvyklá validace a aplikační logika se provádí v implementaci služby.
     *
     * @param playerId identifikátor hráče
     * @param request požadavek na změnu nebo vytvoření registrace
     * @return uložená nebo aktualizovaná registrace převedená do DTO
     */
    MatchRegistrationDTO upsertRegistration(Long playerId, MatchRegistrationRequest request);

    /**
     * Nastavuje registraci hráče do stavu NO_EXCUSED.
     *
     * Metoda se používá pro zaznamenání neoprávněné neúčasti hráče na zápase.
     * K registraci se může uložit administrátorská poznámka popisující důvod
     * takového označení.
     *
     * @param matchId identifikátor zápasu
     * @param playerId identifikátor hráče
     * @param adminNote poznámka administrátora k označení bez omluvy
     * @return aktualizovaná registrace převedená do DTO
     */
    MatchRegistrationDTO markNoExcused(Long matchId, Long playerId, String adminNote);

    /**
     * Ruší stav NO_EXCUSED a nastavuje registraci do stavu EXCUSED.
     *
     * Metoda se používá v situaci, kdy se zpětně uzná omluva hráče,
     * případně dojde k opravě původního rozhodnutí. K registraci se
     * ukládá důvod omluvy a doplňující poznámka.
     *
     * @param matchId identifikátor zápasu
     * @param playerId identifikátor hráče
     * @param excuseReason důvod omluvy
     * @param excuseNote doplňující poznámka k omluvě
     * @return aktualizovaná registrace převedená do DTO
     */
    MatchRegistrationDTO cancelNoExcused(Long matchId,
                                         Long playerId,
                                         ExcuseReason excuseReason,
                                         String excuseNote);

    /**
     * Mění tým hráče v rámci registrace na zápas.
     *
     * Metoda se používá například při vyrovnávání týmů nebo při ruční úpravě
     * složení týmů administrátorem. Výsledkem je aktualizovaná registrace hráče
     * s přiřazeným novým týmem.
     *
     * @param playerId identifikátor hráče
     * @param matchId identifikátor zápasu
     * @return aktualizovaná registrace převedená do DTO
     */
    MatchRegistrationDTO changeRegistrationTeam(Long playerId, Long matchId);

    /**
     * Mění pozici hráče v rámci konkrétního zápasu.
     *
     * Metoda se používá pro ruční nastavení nebo přesuny hráče mezi pozicemi
     * podle aktuálního rozložení v zápase. V implementaci se typicky kontroluje
     * dostupnost pozice a soulad s konfigurací zápasu.
     *
     * @param playerId identifikátor hráče
     * @param matchId identifikátor zápasu
     * @param positionInMatch cílová pozice hráče v daném zápase
     * @return aktualizovaná registrace převedená do DTO
     */
    MatchRegistrationDTO changeRegistrationPosition(Long playerId,
                                                    Long matchId,
                                                    PlayerPosition positionInMatch);

    /**
     * Provádí administrátorskou změnu stavu registrace.
     *
     * Metoda umožňuje nastavení libovolného cílového stavu registrace
     * podle aplikační logiky. Používá se například při ručním zásahu
     * administrátora nebo opravě chybných registrací.
     *
     * @param matchId identifikátor zápasu
     * @param playerId identifikátor hráče
     * @param status cílový stav registrace
     * @return aktualizovaná registrace převedená do DTO
     */
    MatchRegistrationDTO updateStatus(Long matchId,
                                      Long playerId,
                                      PlayerMatchStatus status);

    /**
     * Přepočítává stavy REGISTERED a RESERVED podle aktuální kapacity zápasu.
     *
     * Metoda se používá při změně kapacity zápasu nebo po hromadných změnách
     * registrací. Implementace obvykle rozhoduje, kteří hráči zůstanou
     * ve stavu REGISTERED a kteří budou přesunuti do stavu RESERVED.
     *
     * @param matchId identifikátor zápasu
     */
    void recalcStatusesForMatch(Long matchId);

    /**
     * Povyšuje kandidáty ze stavu RESERVED do stavu REGISTERED
     * při navýšení kapacity zápasu.
     *
     * Metoda se používá v situaci, kdy dojde k uvolnění míst v zápase
     * (například odhlášením hráče) nebo ke zvýšení kapacity zápasu.
     * Implementace vybírá vhodné kandidáty z rezervních hráčů
     * s ohledem na tým a případně pozici.
     *
     * @param matchId identifikátor zápasu
     * @param freedTeam tým, ve kterém se místo uvolnilo nebo preferovaný tým
     * @param freedPosition pozice, která se uvolnila, nebo null, pokud se nemá rozlišovat
     * @param slotsCount počet nových míst, která se mají obsadit ze stavu RESERVED
     */
    void promoteReservedCandidatesForCapacityIncrease(Long matchId,
                                                      Team freedTeam,
                                                      PlayerPosition freedPosition,
                                                      int slotsCount);

    /**
     * Odesílá SMS zprávu všem hráčům ve stavu REGISTERED,
     * kteří mají povolené SMS notifikace.
     *
     * Metoda se používá pro hromadné informování hráčů o změnách
     * souvisejících s konkrétním zápasem, například při změně času
     * nebo místa konání. Implementace typicky využívá notifikační
     * nebo SMS službu.
     *
     * @param matchId identifikátor zápasu
     */
    void sendSmsToRegisteredPlayers(Long matchId);
}