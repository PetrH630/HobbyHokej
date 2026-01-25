package cz.phsoft.hokej.models.dto.mappers;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.Team;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MatchRegistrationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "match", source = "match")
    @Mapping(target = "player", source = "player")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "excuseReason", source = "excuseReason")
    @Mapping(target = "excuseNote", source = "excuseNote")
    @Mapping(target = "team", source = "team")
    @Mapping(target = "adminNote", source = "adminNote")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    MatchRegistrationEntity toEntity(
            MatchEntity match,
            PlayerEntity player,
            PlayerMatchStatus status,
            ExcuseReason excuseReason,
            String excuseNote,
            Team team,
            String adminNote,
            String createdBy
    );

    // entity → DTO (volitelné)
    @Mapping(target = "matchId", source = "match.id")
    @Mapping(target = "playerId", source = "player.id")
    MatchRegistrationDTO toDTO(MatchRegistrationEntity entity);

    List<MatchRegistrationDTO> toDTOList(List<MatchRegistrationEntity> entities);
}
