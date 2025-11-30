package cz.phsoft.hokej.models.dto.mappers;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

            // Entity → DTO
        @Mapping(source = "playerId", target = "id")
        @Mapping(target = "fullName", ignore = true) // fullName se generuje v DTO
        PlayerDTO toDTO(PlayerEntity entity);

        // DTO → Entity
        @Mapping(source = "id", target = "playerId")
        PlayerEntity toEntity(PlayerDTO dto);

        // Aktualizace DTO (ignorujeme fullName)
        @Mapping(target = "fullName", ignore = true)
        void updatePlayerDTO(PlayerDTO source, @MappingTarget PlayerDTO target);

        // Aktualizace Entity
        @Mapping(source = "id", target = "playerId")
        void updatePlayerEntity(PlayerDTO source, @MappingTarget PlayerEntity target);
    }
