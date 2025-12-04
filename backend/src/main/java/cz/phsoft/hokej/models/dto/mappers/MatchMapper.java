package cz.phsoft.hokej.models.dto.mappers;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.models.dto.MatchDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MatchMapper {

    MatchDTO toDTO(MatchEntity entity);

    MatchEntity toEntity(MatchDTO dto);

    void updateEntity(MatchDTO dto, @MappingTarget MatchEntity entity);
}
