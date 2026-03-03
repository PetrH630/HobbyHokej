package cz.phsoft.hokej.user.services;

import cz.phsoft.hokej.user.dto.AppUserHistoryDTO;

import java.util.List;

/**
 * Servisní rozhraní pro práci s historií uživatelských účtů.
 *
 * Slouží k načítání historických záznamů uživatele
 * pro auditní a přehledové účely. Historické záznamy
 * jsou typicky vytvářeny databázovými triggery a
 * následně převáděny na DTO objekty pomocí mapperu.
 *
 * Rozhraní je implementováno servisní třídou,
 * která zajišťuje komunikaci s repozitářem
 * a převod entit na DTO.
 */
public interface AppUserHistoryService {

    /**
     * Vrátí historii změn uživatele podle jeho e-mailové adresy.
     *
     * Metoda se používá pro zobrazení auditní stopy nad účtem
     * v administraci nebo uživatelském detailu.
     *
     * @param email e-mailová adresa uživatele
     * @return seznam historických záznamů uživatele
     */
    List<AppUserHistoryDTO> getHistoryForUser(String email);

    /**
     * Vrátí historii změn uživatele podle jeho identifikátoru.
     *
     * Metoda se používá v situacích, kdy je účet jednoznačně
     * identifikován interním ID a není vhodné pracovat s e-mailem.
     *
     * @param id identifikátor uživatele
     * @return seznam historických záznamů uživatele
     */
    List<AppUserHistoryDTO> getHistoryForUser(Long id);
}