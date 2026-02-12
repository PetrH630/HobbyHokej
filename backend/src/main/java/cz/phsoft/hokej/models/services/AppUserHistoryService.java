package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.models.dto.AppUserHistoryDTO;

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
     * @param email e-mailová adresa uživatele
     * @return seznam historických záznamů uživatele
     */
    List<AppUserHistoryDTO> getHistoryForUser(String email);

    /**
     * Vrátí historii změn uživatele podle jeho identifikátoru.
     *
     * @param id identifikátor uživatele
     * @return seznam historických záznamů uživatele
     */
    List<AppUserHistoryDTO> getHistoryForUser(Long id);
}
