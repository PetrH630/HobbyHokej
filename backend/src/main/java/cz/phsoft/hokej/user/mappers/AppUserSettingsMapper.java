package cz.phsoft.hokej.user.mappers;

import cz.phsoft.hokej.user.entities.AppUserSettingsEntity;
import cz.phsoft.hokej.user.dto.AppUserSettingsDTO;
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

    AppUserSettingsDTO toDTO(AppUserSettingsEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(AppUserSettingsDTO dto,
                             @MappingTarget AppUserSettingsEntity entity);
}