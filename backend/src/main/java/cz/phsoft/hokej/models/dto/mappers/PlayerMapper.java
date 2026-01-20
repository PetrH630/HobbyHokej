package cz.phsoft.hokej.models.dto.mappers;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

        // Entity → DTO (heslo se neposílá)
        @Mapping(source = "nickname", target = "nickName")
        @Mapping(target = "fullName", ignore = true) // generuje se v DTO
        PlayerDTO toDTO(PlayerEntity entity);

        // DTO → Entity (heslo se mapuje, pokud existuje)
        @Mapping(source = "nickName", target = "nickname")   // KLÍČOVÉ
        @Mapping(target = "fullName", ignore = true)         // generuje si Entity sama
        @Mapping(target = "user", ignore = true)             // nastavuješ v service
        @Mapping(
                target = "status",
                expression = "java(dto.getStatus() != null ? dto.getStatus() : cz.phsoft.hokej.data.enums.PlayerStatus.PENDING)"
        )
        PlayerEntity toEntity(PlayerDTO dto);

        // Aktualizace existujícího DTO (ignorujeme fullName)
        @Mapping(target = "fullName", ignore = true)
        @Mapping(target = "id", ignore = true)
        void updatePlayerDTO(PlayerDTO source, @MappingTarget PlayerDTO target);

        // Aktualizace existující Entity (heslo se mapuje, fullName není pole v Entity)
        @Mapping(source = "nickName", target = "nickname")   // i tady je důležité
        @Mapping(target = "fullName", ignore = true)
        @Mapping(target = "user", ignore = true)
        @Mapping(
                target = "status",
                expression = "java(source.getStatus() != null ? source.getStatus() : target.getStatus())"
        )
        void updatePlayerEntity(PlayerDTO source, @MappingTarget PlayerEntity target);

    List<PlayerDTO> toDTOList(List<PlayerEntity> players);


}
