package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.NotificationSettings;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Mapper pro převod hráče mezi entitním
 * a DTO modelem.
 */
@Mapper(componentModel = "spring")
public interface PlayerMapper {

    @Mapping(target = "fullName", ignore = true)
    PlayerDTO toDTO(PlayerEntity entity);

    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "playerStatus", defaultValue = "PENDING")
    PlayerEntity toEntity(PlayerDTO dto);

    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updatePlayerDTO(PlayerDTO source, @MappingTarget PlayerDTO target);

    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updatePlayerEntity(PlayerDTO source, @MappingTarget PlayerEntity target);

    List<PlayerDTO> toDTOList(List<PlayerEntity> players);
}
