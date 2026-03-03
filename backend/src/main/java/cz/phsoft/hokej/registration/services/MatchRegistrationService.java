package cz.phsoft.hokej.registration.services;

import cz.phsoft.hokej.registration.enums.ExcuseReason;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.registration.dto.MatchRegistrationRequest;

import java.util.List;

/**
 * Rozhraní, které se používá pro správu registrací hráčů na zápasy.
 *
 * Definuje kontrakt pro práci s účastí hráčů na zápasech
 * z pohledu business logiky aplikace. Poskytuje operace pro
 * vytvoření nebo změnu registrace, získávání přehledů a
 * administrativní zásahy do stavů registrací.
 *
 * Rozhraní pracuje s DTO objekty a odděluje business logiku
 * od persistence vrstvy. Implementace je odpovědná za validace,
 * přechody stavů registrací a případné navazující akce
 * (notifikace, audit, přepočty kapacit).
 */
public interface MatchRegistrationService {

    /**
     * Vytvoří nebo aktualizuje registraci hráče na zápas.
     *
     * Metoda slouží jako jednotný vstupní bod pro reakci hráče
     * na zápas. Registrace se podle potřeby vytvoří nebo upraví.
     * Implementace zajišťuje validaci vstupních dat, kontrolu
     * povolených přechodů stavů a uložení výsledné registrace.
     *
     * Typickými scénáři jsou přihlášení hráče k zápasu,
     * odhlášení nebo omluva z účasti.
     *
     * @param playerId ID hráče, který reaguje na zápas
     * @param request  požadavek obsahující data o registraci
     * @return DTO reprezentující výsledný stav registrace
     */
    MatchRegistrationDTO upsertRegistration(
            Long playerId,
            MatchRegistrationRequest request
    );

    /**
     * Vrátí seznam registrací pro konkrétní zápas.
     *
     * Slouží pro přehled účasti na daném zápase,
     * jak pro administraci, tak pro prezentační vrstvu.
     *
     * @param matchId ID zápasu
     * @return seznam registrací hráčů k danému zápasu
     */
    List<MatchRegistrationDTO> getRegistrationsForMatch(Long matchId);

    /**
     * Vrátí seznam registrací pro více zápasů.
     *
     * Metoda se typicky používá pro hromadné přehledy
     * nebo statistiky napříč více zápasy.
     *
     * @param matchIds seznam ID zápasů
     * @return seznam registrací pro zadané zápasy
     */
    List<MatchRegistrationDTO> getRegistrationsForMatches(List<Long> matchIds);

    /**
     * Vrátí všechny registrace v systému omezené
     * na relevantní sezónu podle implementace.
     *
     * Metoda se používá zejména pro administrátorské přehledy
     * a agregované statistiky.
     *
     * @return seznam všech registrací
     */
    List<MatchRegistrationDTO> getAllRegistrations();

    /**
     * Vrátí seznam registrací konkrétního hráče.
     *
     * Slouží pro přehled účasti hráče v jednotlivých zápasech
     * a pro potřeby administrace nebo osobního přehledu hráče.
     *
     * @param playerId ID hráče
     * @return seznam registrací daného hráče
     */
    List<MatchRegistrationDTO> getRegistrationsForPlayer(Long playerId);

    /**
     * Vrátí seznam hráčů, kteří dosud nereagovali na daný zápas.
     *
     * Metoda se používá například pro připomínkové notifikace
     * nebo pro přehledy nevyřešené účasti v administraci.
     *
     * @param matchId ID zápasu
     * @return seznam hráčů bez reakce na zápas
     */
    List<PlayerDTO> getNoResponsePlayers(Long matchId);

    /**
     * Přepočítá stavy registrací pro daný zápas.
     *
     * Metoda slouží k zajištění konzistence stavů registrovaných
     * a rezervních hráčů podle kapacity zápasu, typicky po změnách
     * provedených administrátorem nebo po změně parametrů zápasu.
     *
     * @param matchId ID zápasu
     */
    void recalcStatusesForMatch(Long matchId);

    /**
     * Změní stav registrace hráče na zápas.
     *
     * Metoda se používá převážně v administrátorském kontextu,
     * kdy je nutné ručně upravit stav registrace. Nastavení
     * stavu neomluvené neúčasti (NO_EXCUSED) může mít vlastní
     * specializovanou logiku řešenou samostatnými metodami.
     *
     * @param matchId  ID zápasu
     * @param playerId ID hráče
     * @param status   nový stav registrace
     * @return DTO reprezentující aktualizovanou registraci
     */
    MatchRegistrationDTO updateStatus(
            Long matchId,
            Long playerId,
            PlayerMatchStatus status
    );

    /**
     * Označí hráče jako neomluveného pro konkrétní zápas.
     *
     * Metoda se používá v administrátorském kontextu po vyhodnocení
     * účasti na zápase. případná původní omluva se odstraní
     * a registrace se nastaví do stavu NO_EXCUSED včetně poznámky
     * administrátora.
     *
     * @param matchId   ID zápasu
     * @param playerId  ID hráče
     * @param adminNote poznámka administrátora k neomluvené neúčasti
     * @return DTO reprezentující aktualizovanou registraci
     */
    MatchRegistrationDTO markNoExcused(
            Long matchId,
            Long playerId,
            String adminNote
    );

    /**
     * Zruší stav neomluvené neúčasti a nastaví omluvu hráče.
     *
     * Metoda se používá v situacích, kdy je dodatečně uznáno,
     * že hráč měl platný důvod absence. Stav registrace se změní
     * z NO_EXCUSED na omluvený stav a uloží se důvod omluvy
     * a poznámka.
     *
     * @param matchId       ID zápasu
     * @param playerId      ID hráče
     * @param excuseReason  důvod omluvy hráče
     * @param excuseNote    poznámka k omluvě
     * @return DTO reprezentující aktualizovanou registraci
     */
    MatchRegistrationDTO cancelNoExcused(Long matchId,
                                         Long playerId,
                                         ExcuseReason excuseReason,
                                         String excuseNote);

    /**
     * Změní tým hráče v rámci registrace na daný zápas.
     *
     * Konkrétní logika změny týmu (například přepnutí
     * mezi DARK a LIGHT) je definována v implementaci.
     * Metoda může souviset s vyvažováním týmů nebo
     * s administrativní korekcí přiřazení.
     *
     * @param matchId  ID zápasu
     * @param playerId ID hráče
     * @return DTO reprezentující registraci po změně týmu
     */
    MatchRegistrationDTO changeRegistrationTeam(Long matchId,
                                                Long playerId);

    /**
     * Změní pozici hráče v registraci na konkrétní zápas.
     *
     * Metoda se používá při ručním nebo automatickém přesunu
     * hráče na jinou pozici v rámci zápasu, například při změně
     * rozestavení nebo doplnění sestavy.
     *
     * @param playerId        ID hráče
     * @param matchId         ID zápasu
     * @param positionInMatch cílová pozice hráče v zápase
     * @return DTO reprezentující registraci po změně pozice
     */
    MatchRegistrationDTO changeRegistrationPosition(Long playerId,
                                                    Long matchId,
                                                    PlayerPosition positionInMatch);

    /**
     * Povýší kandidáty v rezervním stavu na registrované hráče
     * při navýšení kapacity zápasu.
     *
     * Metoda se používá po uvolnění míst v sestavě, například
     * po odhlášení hráče nebo změně kapacity. Rezervní hráči
     * jsou povyšováni podle definovaných pravidel, typicky
     * podle pořadí registrace, týmu a pozice.
     *
     * @param matchId      ID zápasu
     * @param freedTeam    tým, ve kterém byla kapacita uvolněna
     * @param freedPosition pozice, na které bylo místo uvolněno
     * @param slotsCount   počet nově dostupných míst
     */
    void promoteReservedCandidatesForCapacityIncrease(Long matchId,
                                                      Team freedTeam,
                                                      PlayerPosition freedPosition,
                                                      int slotsCount);

}