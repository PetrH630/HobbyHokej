package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.SeasonEntity;
import cz.phsoft.hokej.models.dto.SeasonDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper pro převod sezón mezi entitním
 * a DTO modelem.
 */
@Mapper(componentModel = "spring")
public interface SeasonMapper {

    SeasonDTO toDTO(SeasonEntity entity);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    SeasonEntity toEntity(SeasonDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    void updateEntityFromDTO(SeasonDTO dto,
                             @MappingTarget SeasonEntity entity);
}
