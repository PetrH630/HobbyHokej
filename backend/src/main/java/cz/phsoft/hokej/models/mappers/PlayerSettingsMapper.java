package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.PlayerSettingsEntity;
import cz.phsoft.hokej.models.dto.PlayerSettingsDTO;
import org.mapstruct.*;

/**
 * Mapper pro převod hráčských nastavení
 * mezi entitou a DTO.
 */
@Mapper(componentModel = "spring")
public interface PlayerSettingsMapper {

    PlayerSettingsDTO toDTO(PlayerSettingsEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(PlayerSettingsDTO dto,
                             @MappingTarget PlayerSettingsEntity entity);
}
