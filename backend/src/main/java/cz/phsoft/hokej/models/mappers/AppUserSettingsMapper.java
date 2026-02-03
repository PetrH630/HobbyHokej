package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.AppUserSettingsEntity;
import cz.phsoft.hokej.models.dto.AppUserSettingsDTO;
import org.mapstruct.*;

/**
 * Mapper pro převod mezi entitou uživatelského nastavení
 * a její DTO reprezentací.
 *
 * Slouží pro správu preferencí uživatele bez zásahu
 * do samotné identity uživatelského účtu.
 */
@Mapper(componentModel = "spring")
public interface AppUserSettingsMapper {

    /**
     * Převede entitu nastavení na DTO.
     *
     * @param entity entita nastavení
     * @return DTO nastavení
     */
    AppUserSettingsDTO toDTO(AppUserSettingsEntity entity);

    /**
     * Aktualizuje existující entitu nastavení hodnotami z DTO.
     *
     * Null hodnoty jsou ignorovány, aby nedošlo
     * k nechtěnému přepsání existujících dat.
     *
     * @param dto    zdrojové DTO
     * @param entity cílová entita
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(AppUserSettingsDTO dto,
                             @MappingTarget AppUserSettingsEntity entity);
}
