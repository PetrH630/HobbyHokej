package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;

import java.util.List;

/**
 * Rozhraní pro správu hráčů v aplikaci.
 *
 * Rozhraní definuje kontrakt pro práci s hráči z pohledu business logiky,
 * včetně jejich vytváření, úprav, schvalování a vazby na uživatele.
 *
 * Odpovědnosti:
 * - správa životního cyklu hráčů (vytvoření, úprava, smazání),
 * - správa vazby hráčů na uživatelské účty,
 * - řízení stavu hráče (čekající, schválený, zamítnutý),
 * - správa „aktuálního hráče“ v kontextu uživatele.
 *
 * Rozhraní se používá:
 * - v controllerech pro hráče a administraci,
 * - v dalších service třídách, které potřebují pracovat s hráči na DTO úrovni.
 */
public interface PlayerService {

    /**
     * Vrátí seznam všech hráčů v systému.
     *
     * Metoda se používá typicky v administrátorských přehledech.
     *
     * @return seznam všech hráčů ve formě {@link PlayerDTO}
     */
    List<PlayerDTO> getAllPlayers();

    /**
     * Vrátí hráče podle jeho ID.
     *
     * @param id ID hráče
     * @return hráč ve formě {@link PlayerDTO}
     */
    PlayerDTO getPlayerById(Long id);

    /**
     * Vytvoří nového hráče bez explicitní vazby na uživatele.
     *
     * Typicky se používá v administraci pro ruční založení hráče.
     *
     * @param player data nového hráče
     * @return vytvořený hráč ve formě {@link PlayerDTO}
     */
    PlayerDTO createPlayer(PlayerDTO player);

    /**
     * Vytvoří nového hráče a přiřadí jej ke konkrétnímu uživateli.
     *
     * Metoda se používá v případech, kdy je hráč vytvářen
     * v kontextu již existujícího uživatelského účtu.
     *
     * @param dto       data nového hráče
     * @param userEmail email uživatele, ke kterému má být hráč přiřazen
     * @return vytvořený hráč ve formě {@link PlayerDTO}
     */
    PlayerDTO createPlayerForUser(PlayerDTO dto, String userEmail);

    /**
     * Aktualizuje údaje existujícího hráče.
     *
     * Metoda aktualizuje základní identifikační a kontaktní údaje
     * i parametry hráče (typ, tým, status).
     *
     * @param id     ID hráče, který má být aktualizován
     * @param player nové hodnoty hráče
     * @return aktualizovaný hráč ve formě {@link PlayerDTO}
     */
    PlayerDTO updatePlayer(Long id, PlayerDTO player);

    /**
     * Odstraní hráče ze systému.
     *
     * Typicky se používá v administraci. Návratová hodnota informuje
     * o úspěchu operace ve formě {@link SuccessResponseDTO}.
     *
     * @param id ID hráče, který má být odstraněn
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO deletePlayer(Long id);

    /**
     * Vrátí seznam hráčů přiřazených ke konkrétnímu uživateli.
     *
     * Metoda se používá například při zobrazení hráčů přihlášeného uživatele.
     *
     * @param email email uživatele
     * @return seznam hráčů daného uživatele ve formě {@link PlayerDTO}
     */
    List<PlayerDTO> getPlayersByUser(String email);

    /**
     * Schválí hráče.
     *
     * Po schválení je hráč považován za aktivního
     * a může se účastnit zápasů podle dalších pravidel aplikace.
     *
     * @param id ID hráče
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO approvePlayer(Long id);

    /**
     * Zamítne hráče.
     *
     * Zamítnutý hráč se nepovažuje za aktivního
     * a nemůže se účastnit zápasů.
     *
     * @param id ID hráče
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO rejectPlayer(Long id);

    /**
     * Nastaví aktuálního hráče pro konkrétního uživatele.
     *
     * Metoda slouží k explicitnímu výběru hráče v případě,
     * že má uživatel přiřazeno více hráčů.
     *
     * @param userEmail email uživatele
     * @param playerId  ID hráče, který má být nastaven jako aktuální
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO setCurrentPlayerForUser(String userEmail, Long playerId);

    /**
     * Automaticky zvolí aktuálního hráče pro daného uživatele
     * podle jeho nastavení (AppUserSettings.playerSelectionMode).
     *
     * Typické použití:
     * - po přihlášení uživatele,
     * - při explicitním volání z frontendu (například tlačítko „Vybrat výchozího hráče“).
     *
     * @param userEmail email přihlášeného uživatele
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO autoSelectCurrentPlayerForUser(String userEmail);

    /**
     * Změní přiřazeného uživatele k existujícímu hráči.
     *
     * Metoda slouží k administrátorské úpravě vazby mezi hráčem a
     * uživatelským účtem, například při opravě chybného přiřazení
     * nebo převodu hráče pod jiný uživatelský účet.
     *
     * Implementace mění pouze vazbu hráč → uživatel,
     * ostatní business logika (například změna current player)
     * je ponechána volajícímu.
     *
     * @param id        ID hráče, kterému se má změnit přiřazený uživatel
     * @param newUserId ID nového uživatele, ke kterému má být hráč přiřazen
     */
    void changePlayerUser(Long id, Long newUserId);

}
