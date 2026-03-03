package cz.phsoft.hokej.user.mappers;

import cz.phsoft.hokej.user.entities.AppUserSettingsEntity;
import cz.phsoft.hokej.user.dto.AppUserSettingsDTO;
import org.mapstruct.*;

/**
 * Mapper pro převod mezi entitou uživatelského nastavení
 * a její DTO reprezentací.
 *
 * Slouží pro správu preferencí uživatele bez zásahu
 * do samotné identity uživatelského účtu. Entita
 * AppUserSettingsEntity zůstává perzistenčním modelem,
 * DTO se používá pro komunikaci s API vrstvou.
 */
@Mapper(componentModel = "spring")
public interface AppUserSettingsMapper {

    /**
     * Převede entitu nastavení uživatele na DTO.
     *
     * Metoda se používá při načítání nastavení pro aktuálně
     * přihlášeného uživatele. Všechny relevantní vlastnosti
     * se přenesou do DTO, které je vráceno controllerem.
     *
     * @param entity entita nastavení uživatele
     * @return DTO reprezentace nastavení uživatele
     */
    AppUserSettingsDTO toDTO(AppUserSettingsEntity entity);

    /**
     * Aktualizuje existující entitu nastavení hodnotami z DTO.
     *
     * Díky nastavení nullValuePropertyMappingStrategy na IGNORE
     * se nenahrazují vlastnosti, které jsou v DTO null. Metoda
     * se používá pro částečné aktualizace nastavení (PATCH)
     * bez nutnosti posílat kompletní objekt.
     *
     * Vazba na uživatele ani primární klíč se tímto mapováním
     * nemění a jsou spravovány v servisní vrstvě.
     *
     * @param dto    zdrojové DTO s novými hodnotami nastavení
     * @param entity cílová entita nastavení, která má být aktualizována
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(AppUserSettingsDTO dto,
                             @MappingTarget AppUserSettingsEntity entity);
}