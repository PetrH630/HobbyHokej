package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.enums.PlayerPosition;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.requests.MatchRegistrationRequest;

import java.util.List;

/**
 * Rozhraní se používá pro správu registrací hráčů na zápasy.
 * <p>
 * Definuje kontrakt pro práci s účastí hráčů na zápasech
 * z pohledu business logiky aplikace. Poskytuje operace pro
 * vytvoření nebo změnu registrace, získávání přehledů a
 * administrativní zásahy do stavů registrací.
 * <p>
 * Rozhraní pracuje s DTO objekty a odděluje business logiku
 * od persistence vrstvy. Implementace je odpovědná za validace
 * a přechody stavů registrací.
 */
public interface MatchRegistrationService {

    /**
     * Vytvoří nebo aktualizuje registraci hráče na zápas.
     * <p>
     * Metoda slouží jako jednotný vstupní bod pro reakci hráče
     * na zápas. Registrace se podle potřeby vytvoří nebo upraví.
     * Implementace zajišťuje validaci vstupních dat, kontrolu
     * povolených přechodů stavů a uložení výsledné registrace.
     * <p>
     * Typickým scénářem je přihlášení hráče k zápasu, odhlášení
     * nebo omluva z účasti.
     *
     * @param playerId ID hráče, který reaguje na zápas
     * @param request  požadavek obsahující data o registraci
     * @return DTO reprezentace výsledného stavu registrace
     */
    MatchRegistrationDTO upsertRegistration(
            Long playerId,
            MatchRegistrationRequest request
    );

    /**
     * Vrátí seznam registrací pro konkrétní zápas.
     *
     * @param matchId ID zápasu
     * @return seznam registrací hráčů k danému zápasu
     */
    List<MatchRegistrationDTO> getRegistrationsForMatch(Long matchId);

    /**
     * Vrátí seznam registrací pro více zápasů.
     * <p>
     * Metoda se typicky používá pro hromadné přehledy
     * nebo statistiky přes více zápasů.
     *
     * @param matchIds seznam ID zápasů
     * @return seznam registrací pro zadané zápasy
     */
    List<MatchRegistrationDTO> getRegistrationsForMatches(List<Long> matchIds);

    /**
     * Vrátí všechny registrace v systému omezené
     * na relevantní sezónu podle implementace.
     * <p>
     * Metoda se používá zejména pro administrátorské přehledy.
     *
     * @return seznam všech registrací
     */
    List<MatchRegistrationDTO> getAllRegistrations();

    /**
     * Vrátí seznam registrací konkrétního hráče.
     *
     * @param playerId ID hráče
     * @return seznam registrací daného hráče
     */
    List<MatchRegistrationDTO> getRegistrationsForPlayer(Long playerId);

    /**
     * Vrátí seznam hráčů, kteří dosud nereagovali na daný zápas.
     * <p>
     * Metoda se používá například pro připomínkové notifikace
     * nebo pro přehledy nevyřešené účasti.
     *
     * @param matchId ID zápasu
     * @return seznam hráčů bez reakce na zápas
     */
    List<PlayerDTO> getNoResponsePlayers(Long matchId);

    /**
     * Přepočítá stavy registrací pro daný zápas.
     * <p>
     * Metoda slouží k zajištění konzistence stavů registrovaných
     * a rezervních hráčů podle kapacity zápasu, typicky po změnách
     * provedených administrátorem.
     *
     * @param matchId ID zápasu
     */
    void recalcStatusesForMatch(Long matchId);

    /**
     * Změní stav registrace hráče na zápas.
     * <p>
     * Metoda se používá převážně v administrátorském kontextu,
     * kde je nutné ručně upravit stav registrace. Nastavení
     * stavu NO_EXCUSED má vlastní logiku a řeší se samostatně.
     *
     * @param matchId  ID zápasu
     * @param playerId ID hráče
     * @param status   nový stav registrace
     * @return DTO reprezentace aktualizované registrace
     */
    MatchRegistrationDTO updateStatus(
            Long matchId,
            Long playerId,
            PlayerMatchStatus status
    );

    /**
     * Označí hráče jako neomluveného pro konkrétní zápas.
     * <p>
     * Metoda se používá v administrátorském kontextu po vyhodnocení
     * účasti na zápase. Původní omluva se odstraní a registrace
     * se nastaví do stavu NO_EXCUSED včetně poznámky administrátora.
     *
     * @param matchId   ID zápasu
     * @param playerId  ID hráče
     * @param adminNote poznámka administrátora
     * @return DTO reprezentace aktualizované registrace
     */
    MatchRegistrationDTO markNoExcused(
            Long matchId,
            Long playerId,
            String adminNote
    );

    MatchRegistrationDTO cancelNoExcused(Long matchId,
                                         Long playerId,
                                         ExcuseReason excuseReason,
                                         String excuseNote);



    MatchRegistrationDTO changeRegistrationTeam(Long matchId,
                                                Long playerId);

    MatchRegistrationDTO changeRegistrationPosition(Long playerId,
                                                    Long matchId,
                                                    PlayerPosition positionInMatch);


}
