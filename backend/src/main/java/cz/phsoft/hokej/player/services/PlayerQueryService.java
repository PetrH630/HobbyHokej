package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.dto.PlayerDTO;

import java.util.List;

/**
 * Rozhraní servisní vrstvy pro čtecí operace nad hráči.
 *
 * Definuje kontrakt pro získávání informací o hráčích
 * bez provádění změn v perzistentní vrstvě.
 *
 * Rozhraní je určeno pro oddělení čtecí logiky
 * od změnových operací v rámci principu CQRS.
 * Implementace zajišťuje načtení dat z repository vrstvy
 * a jejich převod do DTO reprezentace.
 */
public interface PlayerQueryService {

    /**
     * Vrátí seznam všech hráčů evidovaných v systému.
     *
     * @return seznam hráčů ve formě PlayerDTO
     */
    List<PlayerDTO> getAllPlayers();

    /**
     * Vrátí detail hráče podle jeho identifikátoru.
     *
     * @param id identifikátor hráče
     * @return hráč ve formě PlayerDTO
     */
    PlayerDTO getPlayerById(Long id);

    /**
     * Vrátí seznam hráčů přiřazených ke konkrétnímu uživateli.
     *
     * @param email e-mail uživatele
     * @return seznam hráčů daného uživatele ve formě PlayerDTO
     */
    List<PlayerDTO> getPlayersByUser(String email);
}