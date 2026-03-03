package cz.phsoft.hokej.user.mappers;

import cz.phsoft.hokej.user.entities.AppUserHistoryEntity;
import cz.phsoft.hokej.user.dto.AppUserHistoryDTO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper pro převod historických záznamů uživatelů
 * na jejich DTO reprezentace.
 *
 * Slouží výhradně pro čtecí účely a auditní přehledy.
 * Datová vrstva poskytuje entity AppUserHistoryEntity,
 * které se pomocí tohoto mapperu převádějí do DTO pro API vrstvu.
 */
@Mapper(componentModel = "spring")
public interface AppUserHistoryMapper {

    /**
     * Převede entitu historického záznamu uživatele na DTO.
     *
     * Metoda se používá při načtení jednotlivých záznamů historie
     * z databáze a zobrazení jejich obsahu klientovi.
     *
     * @param entity entita historického záznamu uživatele
     * @return DTO reprezentace historického záznamu
     */
    AppUserHistoryDTO toDTO(AppUserHistoryEntity entity);

    /**
     * Převede seznam entit historických záznamů na seznam DTO.
     *
     * Mapování probíhá položku po položce pomocí metody toDTO.
     * Používá se pro auditní a přehledové obrazovky, kde je
     * potřeba zobrazit celou historii uživatele.
     *
     * @param entities seznam entit historických záznamů
     * @return seznam DTO reprezentací historických záznamů
     */
    List<AppUserHistoryDTO> toDTOList(
            List<AppUserHistoryEntity> entities
    );
}