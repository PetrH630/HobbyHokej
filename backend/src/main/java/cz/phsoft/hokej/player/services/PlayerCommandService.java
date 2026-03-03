package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;

/**
 * Rozhraní servisní vrstvy pro změnové operace nad hráči.
 *
 * Definuje kontrakt pro všechny operace, které mění stav systému
 * ve vztahu k entitě hráče nebo jeho vazbě na uživatele.
 *
 * Rozhraní je součástí oddělení čtecí a zápisové logiky.
 * Implementace zajišťuje validaci vstupních dat, komunikaci
 * s repository vrstvou a případné vyvolání dalších doménových služeb.
 */
public interface PlayerCommandService {

    /**
     * Vytvoří nového hráče.
     *
     * @param dto data nového hráče
     * @return vytvořený hráč ve formě PlayerDTO
     */
    PlayerDTO createPlayer(PlayerDTO dto);

    /**
     * Vytvoří nového hráče a přiřadí jej ke konkrétnímu uživateli.
     *
     * @param dto data nového hráče
     * @param userEmail e-mail uživatele, ke kterému má být hráč přiřazen
     * @return vytvořený hráč ve formě PlayerDTO
     */
    PlayerDTO createPlayerForUser(PlayerDTO dto, String userEmail);

    /**
     * Aktualizuje existujícího hráče.
     *
     * @param id identifikátor hráče
     * @param dto nové hodnoty hráče
     * @return aktualizovaný hráč ve formě PlayerDTO
     */
    PlayerDTO updatePlayer(Long id, PlayerDTO dto);

    /**
     * Odstraní hráče ze systému.
     *
     * @param id identifikátor hráče
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO deletePlayer(Long id);

    /**
     * Schválí hráče a změní jeho stav na aktivní.
     *
     * @param id identifikátor hráče
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO approvePlayer(Long id);

    /**
     * Zamítne hráče a změní jeho stav na neaktivní.
     *
     * @param id identifikátor hráče
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO rejectPlayer(Long id);

    /**
     * Změní přiřazeného uživatele k existujícímu hráči.
     *
     * @param id identifikátor hráče
     * @param newUserId identifikátor nového uživatele
     */
    void changePlayerUser(Long id, Long newUserId);

    /**
     * Nastaví aktuálního hráče pro konkrétního uživatele.
     *
     * @param userEmail e-mail uživatele
     * @param playerId identifikátor hráče, který má být nastaven jako aktuální
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO setCurrentPlayerForUser(String userEmail, Long playerId);

    /**
     * Automaticky zvolí aktuálního hráče pro daného uživatele
     * podle jeho nastavení.
     *
     * @param userEmail e-mail uživatele
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO autoSelectCurrentPlayerForUser(String userEmail);
}