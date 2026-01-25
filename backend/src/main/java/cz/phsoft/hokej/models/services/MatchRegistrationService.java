package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.requests.MatchRegistrationRequest;

import java.util.List;

/**
 * Rozhraní pro správu registrací hráčů na zápasy.
 * <p>
 * Definuje kontrakt pro práci s účastí hráčů na zápasech
 * z pohledu business logiky aplikace.
 * </p>
 *
 * Význam v aplikaci:
 * <ul>
 *     <li>umožňuje hráčům reagovat na zápasy (přihlášení, odhlášení, omluva),</li>
 *     <li>poskytuje přehled registrací pro zápasy i jednotlivé hráče,</li>
 *     <li>zajišťuje konzistenci stavů registrací.</li>
 * </ul>
 *
 * Architektonické zásady:
 * <ul>
 *     <li>pracuje výhradně s DTO objekty, nikoliv s entitami,</li>
 *     <li>odděluje business logiku registrací od persistence vrstvy,</li>
 *     <li>implementace je zodpovědná za validace a přechody stavů.</li>
 * </ul>
 *
 * Použití:
 * <ul>
 *     <li>využívá se v controllerech a dalších business službách,</li>
 *     <li>je klíčovou součástí workflow práce se zápasy.</li>
 * </ul>
 */
public interface MatchRegistrationService {

    /**
     * Vytvoří nebo aktualizuje registraci hráče na zápas.
     * <p>
     * Metoda slouží jako jednotný vstupní bod pro reakci hráče
     * na zápas (tzv. upsert – insert nebo update).
     * </p>
     *
     * Typické scénáře:
     * <ul>
     *     <li>přihlášení hráče k zápasu,</li>
     *     <li>odhlášení hráče ze zápasu,</li>
     *     <li>omluva hráče.</li>
     * </ul>
     *
     * Implementace zajišťuje:
     * <ul>
     *     <li>validaci vstupních dat,</li>
     *     <li>kontrolu povolených přechodů stavů,</li>
     *     <li>vytvoření nebo úpravu registrace.</li>
     * </ul>
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
     * Typicky se používá pro hromadné přehledy nebo statistiky.
     * </p>
     *
     * @param matchIds seznam ID zápasů
     * @return seznam registrací pro zadané zápasy
     */
    List<MatchRegistrationDTO> getRegistrationsForMatches(List<Long> matchIds);

    /**
     * Vrátí všechny registrace v systému.
     * <p>
     * Typicky určeno pro administrátorské přehledy.
     * </p>
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
     * Používá se zejména:
     * </p>
     * <ul>
     *     <li>pro připomínkové notifikace,</li>
     *     <li>pro přehledy nevyřešené účasti.</li>
     * </ul>
     *
     * @param matchId ID zápasu
     * @return seznam hráčů bez reakce
     */
    List<PlayerDTO> getNoResponsePlayers(Long matchId);

    /**
     * Přepočítá stavy registrací pro daný zápas.
     * <p>
     * Metoda slouží k zajištění konzistence stavů
     * (např. po administrátorském zásahu).
     * </p>
     *
     * @param matchId ID zápasu
     */
    void recalcStatusesForMatch(Long matchId);

    /**
     * Změní stav registrace hráče na zápas.
     * <p>
     * Typicky se používá v administrátorském kontextu,
     * kde je nutné ručně upravit stav registrace.
     * </p>
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
     * Označí hráče jako „neomluveného“ pro konkrétní zápas.
     * <p>
     * Používá se zejména v administrátorském kontextu
     * po vyhodnocení účasti na zápase.
     * </p>
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
}
