package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerInactivityPeriodEntity;
import cz.phsoft.hokej.models.dto.PlayerInactivityPeriodDTO;
import org.mapstruct.*;

/**
 * Mapper pro převod období neaktivity hráče
 * mezi entitním a DTO modelem.
 */
@Mapper(componentModel = "spring")
public interface PlayerInactivityPeriodMapper {

    @Mapping(target = "playerId", source = "player.id")
    PlayerInactivityPeriodDTO toDTO(PlayerInactivityPeriodEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "player", ignore = true)
    PlayerInactivityPeriodEntity toEntity(PlayerInactivityPeriodDTO dto,
                                          @Context PlayerEntity player);

    @Mapping(target = "player", ignore = true)
    void updateEntityFromDto(PlayerInactivityPeriodDTO dto,
                             @MappingTarget PlayerInactivityPeriodEntity entity);

    /**
     * Tovární metoda pro vytvoření nové entity
     * s navázaným hráčem z kontextu.
     */
    @ObjectFactory
    default PlayerInactivityPeriodEntity createEntity(
            PlayerInactivityPeriodDTO dto,
            @Context PlayerEntity player
    ) {
        PlayerInactivityPeriodEntity entity = new PlayerInactivityPeriodEntity();
        entity.setPlayer(player);
        return entity;
    }
}
