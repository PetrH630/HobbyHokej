package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.PlayerSettingsEntity;
import cz.phsoft.hokej.models.dto.PlayerSettingsDTO;
import org.mapstruct.*;

/**
 * Mapper mezi PlayerSettingsEntity a PlayerSettingsDTO.
 */
@Mapper(componentModel = "spring")
public interface PlayerSettingsMapper {

    // ENTITY -> DTO
    PlayerSettingsDTO toDTO(PlayerSettingsEntity entity);

    // DTO -> ENTITY (UPDATE)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(PlayerSettingsDTO dto,
                             @MappingTarget PlayerSettingsEntity entity);
}
