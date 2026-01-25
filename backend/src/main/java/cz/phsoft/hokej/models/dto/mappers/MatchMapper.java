package cz.phsoft.hokej.models.dto.mappers;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.models.dto.MatchDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MatchMapper {

    @Mapping(source = "season.id", target = "seasonId")
    MatchDTO toDTO(MatchEntity entity);

    @Mapping(target = "season", ignore = true)
    MatchEntity toEntity(MatchDTO dto);

    @Mapping(target = "season", ignore = true)
    void updateEntity(MatchDTO dto, @MappingTarget MatchEntity entity);
}
