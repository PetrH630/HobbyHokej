package cz.phsoft.hokej.models.dto.mappers;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.models.dto.AppUserDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.PlayerSummaryDTO;
import cz.phsoft.hokej.models.dto.RegisterUserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AppUserMapper {
    @Mapping(target = "players", source = "players")
    AppUserDTO toDTO(AppUserEntity entity);

    List<AppUserDTO> toDtoList(List<AppUserEntity> entities);

    @Mapping(source = "nickname", target = "nickName")
    @Mapping(target = "fullName", ignore = true)

    PlayerDTO toPlayerDTO(PlayerEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // nastavuje service
    @Mapping(target = "role", ignore = true)     // nastavuje service
    @Mapping(target = "enabled", ignore = true)  // nastavuje service
    @Mapping(target = "players", ignore = true)  // vazba se řeší jinde
    AppUserEntity fromRegisterDto(RegisterUserDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "players", ignore = true)

    void updateEntityFromDto(AppUserDTO dto, @MappingTarget AppUserEntity entity);
}
