package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;

import java.util.List;

/**
 * Rozhraní pro správu hráčů v aplikační vrstvě.
 *
 * Definuje kontrakt business logiky nad entitou hráče.
 * Odpovídá za řízení životního cyklu hráče, správu jeho vazby
 * na uživatelský účet a za řízení stavů hráče v systému.
 *
 * Rozhraní je využíváno především controllery a dalšími service
 * třídami, které pracují s hráči na úrovni DTO objektů.
 *
 * Implementace rozhraní zajišťuje validaci vstupních dat,
 * komunikaci s repository vrstvou a případné vyvolání dalších
 * doménových služeb.
 */
public interface PlayerService {

    /**
     * Vrátí seznam všech hráčů evidovaných v systému.
     *
     * Metoda se používá zejména v administrátorských přehledech.
     * Implementace zajišťuje načtení dat z perzistentní vrstvy
     * a jejich převod do DTO reprezentace.
     *
     * @return seznam všech hráčů ve formě PlayerDTO
     */
    List<PlayerDTO> getAllPlayers();

    /**
     * Vrátí detail hráče podle jeho identifikátoru.
     *
     * Implementace zajišťuje ověření existence hráče
     * a jeho převod do DTO reprezentace.
     *
     * @param id identifikátor hráče
     * @return hráč ve formě PlayerDTO
     */
    PlayerDTO getPlayerById(Long id);

    /**
     * Vytvoří nového hráče bez explicitní vazby na uživatelský účet.
     *
     * Metoda se používá typicky v administraci při ručním založení hráče.
     * Implementace zajišťuje validaci vstupních dat, vytvoření entity
     * a její uložení do databáze.
     *
     * @param player data nového hráče
     * @return vytvořený hráč ve formě PlayerDTO
     */
    PlayerDTO createPlayer(PlayerDTO player);

    /**
     * Vytvoří nového hráče a přiřadí jej ke konkrétnímu uživateli.
     *
     * Metoda se používá v situacích, kdy je hráč zakládán
     * v kontextu existujícího uživatelského účtu.
     * Implementace zajišťuje ověření existence uživatele,
     * vytvoření hráče a nastavení vazby hráč–uživatel.
     *
     * @param dto data nového hráče
     * @param userEmail e-mail uživatele, ke kterému má být hráč přiřazen
     * @return vytvořený hráč ve formě PlayerDTO
     */
    PlayerDTO createPlayerForUser(PlayerDTO dto, String userEmail);

    /**
     * Aktualizuje údaje existujícího hráče.
     *
     * Implementace provádí načtení hráče, aktualizaci jeho
     * identifikačních, kontaktních a doménových údajů
     * a následné uložení změn do perzistentní vrstvy.
     *
     * @param id identifikátor hráče, který má být aktualizován
     * @param player nové hodnoty hráče
     * @return aktualizovaný hráč ve formě PlayerDTO
     */
    PlayerDTO updatePlayer(Long id, PlayerDTO player);

    /**
     * Odstraní hráče ze systému.
     *
     * Operace je určena zejména pro administrátorské použití.
     * Implementace zajišťuje ověření existence hráče
     * a jeho odstranění z databáze.
     *
     * @param id identifikátor hráče, který má být odstraněn
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO deletePlayer(Long id);

    /**
     * Vrátí seznam hráčů přiřazených ke konkrétnímu uživateli.
     *
     * Metoda se používá například při zobrazení hráčů
     * přihlášeného uživatele v uživatelském rozhraní.
     * Implementace načítá hráče podle vazby na uživatelský účet.
     *
     * @param email e-mail uživatele
     * @return seznam hráčů daného uživatele ve formě PlayerDTO
     */
    List<PlayerDTO> getPlayersByUser(String email);

    /**
     * Schválí hráče.
     *
     * Po schválení je hráč považován za aktivního
     * a může se účastnit zápasů podle pravidel aplikace.
     * Implementace mění stav hráče a ukládá změnu do databáze.
     *
     * @param id identifikátor hráče
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO approvePlayer(Long id);

    /**
     * Zamítne hráče.
     *
     * Zamítnutý hráč není považován za aktivního
     * a nemůže se účastnit zápasů.
     * Implementace mění stav hráče a ukládá změnu do databáze.
     *
     * @param id identifikátor hráče
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO rejectPlayer(Long id);

    /**
     * Nastaví aktuálního hráče pro konkrétního uživatele.
     *
     * Metoda umožňuje explicitní výběr hráče v případě,
     * že má uživatel přiřazeno více hráčů.
     * Implementace ověřuje vazbu hráče na uživatele
     * a aktualizuje nastavení aktuálního hráče.
     *
     * @param userEmail e-mail uživatele
     * @param playerId identifikátor hráče, který má být nastaven jako aktuální
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO setCurrentPlayerForUser(String userEmail, Long playerId);

    /**
     * Automaticky zvolí aktuálního hráče pro daného uživatele
     * podle jeho nastavení výběru hráče.
     *
     * Metoda se používá například po přihlášení uživatele
     * nebo při explicitním vyvolání z uživatelského rozhraní.
     * Implementace vyhodnocuje uživatelské nastavení
     * a nastavuje odpovídajícího hráče jako aktuálního.
     *
     * @param userEmail e-mail přihlášeného uživatele
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO autoSelectCurrentPlayerForUser(String userEmail);

    /**
     * Změní přiřazeného uživatele k existujícímu hráči.
     *
     * Metoda je určena pro administrátorské zásahy
     * do vazby mezi hráčem a uživatelským účtem.
     * Implementace mění pouze vazbu hráč–uživatel.
     * Ostatní navazující business logika je ponechána volající vrstvě.
     *
     * @param id identifikátor hráče, kterému má být změněn uživatel
     * @param newUserId identifikátor nového uživatele
     */
    void changePlayerUser(Long id, Long newUserId);

}