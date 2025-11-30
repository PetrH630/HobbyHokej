package cz.phsoft.hokej.models.dto.mappers;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.models.dto.MatchDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {PlayerMapper.class})
public interface MatchMapper {

    // Entity → DTO
    @Mapping(source = "matchId", target = "id")
    MatchDTO toDTO(MatchEntity entity);

    // DTO → Entity
    @Mapping(source = "id", target = "matchId")
    MatchEntity toEntity(MatchDTO dto);

    // Aktualizace DTO
    void updateMatchDTO(MatchDTO source, @MappingTarget MatchDTO target);

    // Aktualizace Entity
    void updateMatchEntity(MatchDTO source, @MappingTarget MatchEntity target);
}
