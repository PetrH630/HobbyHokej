package cz.phsoft.hokej.models.dto.mappers;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

        // Entity → DTO (heslo se neposílá)
        @Mapping(target = "fullName", ignore = true) // generuje se v DTO
        PlayerDTO toDTO(PlayerEntity entity);

        // DTO → Entity (heslo se mapuje, pokud existuje)
        PlayerEntity toEntity(PlayerDTO dto);

        // Aktualizace existujícího DTO (ignorujeme fullName)
        @Mapping(target = "fullName", ignore = true)
        void updatePlayerDTO(PlayerDTO source, @MappingTarget PlayerDTO target);

        // Aktualizace existující Entity (heslo se mapuje, fullName není pole v Entity)
        void updatePlayerEntity(PlayerDTO source, @MappingTarget PlayerEntity target);
}
