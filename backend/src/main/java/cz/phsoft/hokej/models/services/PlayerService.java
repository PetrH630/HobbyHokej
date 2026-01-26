package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;

import java.util.List;

/**
 * Rozhraní pro správu hráčů v aplikaci.
 * <p>
 * Definuje kontrakt pro práci s hráči z pohledu business logiky,
 * včetně jejich vytváření, úprav, schvalování a vazby na uživatele.
 * </p>
 *
 * Účel:
 * <ul>
 *     <li>správa životního cyklu hráčů (vytvoření, úprava, smazání),</li>
 *     <li>vazba hráčů na uživatelské účty,</li>
 *     <li>řízení stavu hráče (čekající, schválený, zamítnutý).</li>
 * </ul>
 *
 * Použití:
 * <ul>
 *     <li>využívá se v controllerech a dalších business službách,</li>
 *     <li>pracuje výhradně s DTO objekty.</li>
 * </ul>
 */
public interface PlayerService {

    /**
     * Vrátí seznam všech hráčů v systému.
     * <p>
     * Typicky určeno pro administrátorské přehledy.
     * </p>
     *
     * @return seznam všech hráčů
     */
    List<PlayerDTO> getAllPlayers();

    /**
     * Vrátí hráče podle jeho ID.
     *
     * @param id ID hráče
     * @return hráč ve formě DTO
     */
    PlayerDTO getPlayerById(Long id);

    /**
     * Vytvoří nového hráče.
     * <p>
     * Hráč je po vytvoření obvykle ve stavu čekajícím
     * na schválení administrátorem.
     * </p>
     *
     * @param player data nového hráče
     * @return vytvořený hráč
     */
    PlayerDTO createPlayer(PlayerDTO player);

    /**
     * Vytvoří nového hráče a přiřadí jej ke konkrétnímu uživateli.
     * <p>
     * Používá se v případech, kdy je hráč vytvářen
     * v kontextu již existujícího uživatelského účtu.
     * </p>
     *
     * @param dto       data nového hráče
     * @param userEmail email uživatele, ke kterému má být hráč přiřazen
     * @return vytvořený hráč
     */
    PlayerDTO createPlayerForUser(PlayerDTO dto, String userEmail);

    /**
     * Aktualizuje údaje existujícího hráče.
     *
     * @param id     ID hráče, který má být aktualizován
     * @param player nové hodnoty hráče
     * @return aktualizovaný hráč
     */
    PlayerDTO updatePlayer(Long id, PlayerDTO player);

    /**
     * Odstraní hráče ze systému.
     * <p>
     * Typicky vrací informaci o úspěchu operace
     * ve formě {@link SuccessResponseDTO}.
     * </p>
     *
     * @param id ID hráče, který má být odstraněn
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO deletePlayer(Long id);

    /**
     * Vrátí seznam hráčů přiřazených ke konkrétnímu uživateli.
     *
     * @param email email uživatele
     * @return seznam hráčů daného uživatele
     */
    List<PlayerDTO> getPlayersByUser(String email);

    /**
     * Schválí hráče.
     * <p>
     * Po schválení je hráč považován za aktivního
     * a může se účastnit zápasů.
     * </p>
     *
     * @param id ID hráče
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO approvePlayer(Long id);

    /**
     * Zamítne hráče.
     * <p>
     * Zamítnutý hráč se nemůže účastnit zápasů
     * a není považován za aktivního.
     * </p>
     *
     * @param id ID hráče
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO rejectPlayer(Long id);

    /**
     * Nastaví aktuálního hráče pro konkrétního uživatele.
     * <p>
     * Slouží k explicitnímu výběru hráče v případě,
     * že má uživatel přiřazeno více hráčů.
     * </p>
     *
     * @param userEmail email uživatele
     * @param playerId  ID hráče, který má být nastaven jako aktuální
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO setCurrentPlayerForUser(String userEmail, Long playerId);

    /**
     * Automaticky zvolí aktuálního hráče pro uživatele.
     * <p>
     * Typicky se používá v případech, kdy má uživatel
     * přiřazen pouze jeden hráč.
     * </p>
     *
     * @param userEmail email uživatele
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO autoSelectCurrentPlayerForUser(String userEmail);

    /**
     * TODO
     */
    void changePlayerUser(Long id, Long newUserId);
}
