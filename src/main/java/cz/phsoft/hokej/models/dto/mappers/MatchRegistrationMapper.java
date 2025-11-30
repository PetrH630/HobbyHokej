package cz.phsoft.hokej.models.dto.mappers;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

public interface MatchRegistrationMapper {
    // DTO → Entity
    @Mapping(source = "matchId", target = "match.matchId")
    @Mapping(source = "playerId", target = "player.playerId")
    MatchRegistrationEntity toEntity(MatchRegistrationDTO dto, MatchEntity match, PlayerEntity player);

    // Entity → DTO
    @Mapping(source = "match.matchId", target = "matchId")
    @Mapping(source = "player.playerId", target = "playerId")
    MatchRegistrationDTO toDTO(MatchRegistrationEntity entity);

    // Aktualizace Entity
    void updateEntity(MatchRegistrationDTO dto, @MappingTarget MatchRegistrationEntity entity);
}
