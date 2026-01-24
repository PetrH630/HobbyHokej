package cz.phsoft.hokej.models.dto.mappers;

import cz.phsoft.hokej.data.entities.SeasonEntity;
import cz.phsoft.hokej.models.dto.SeasonDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface SeasonMapper {

        SeasonDTO toDTO(SeasonEntity entity);

        SeasonEntity toEntity(SeasonDTO dto);

        void updateEntityFromDTO(SeasonDTO dto, @MappingTarget SeasonEntity entity);
}

