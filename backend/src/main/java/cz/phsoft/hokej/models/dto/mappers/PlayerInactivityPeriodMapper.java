package cz.phsoft.hokej.models.dto.mappers;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerInactivityPeriodEntity;
import cz.phsoft.hokej.models.dto.PlayerInactivityPeriodDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PlayerInactivityPeriodMapper {

    // Entity -> DTO (OK)
    @Mapping(target = "playerId", source = "player.id")
    PlayerInactivityPeriodDTO toDTO(PlayerInactivityPeriodEntity entity);

    // DTO -> Entity (player řeší @ObjectFactory)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "player", ignore = true)
    PlayerInactivityPeriodEntity toEntity(PlayerInactivityPeriodDTO dto, @Context PlayerEntity player);

    // UPDATE DTO -> existující entity (bez změny player)
    @Mapping(target = "player", ignore = true)
    void updateEntityFromDto(PlayerInactivityPeriodDTO dto, @MappingTarget PlayerInactivityPeriodEntity entity);

    // FACTORY – jediný správný způsob, jak nastavit player z @Context
    @ObjectFactory
    default PlayerInactivityPeriodEntity createEntity(PlayerInactivityPeriodDTO dto, @Context PlayerEntity player) {
        PlayerInactivityPeriodEntity entity = new PlayerInactivityPeriodEntity();
        entity.setPlayer(player);
        return entity;
    }
}
