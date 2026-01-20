package cz.phsoft.hokej.models.dto.mappers;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.models.dto.AppUserDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.PlayerSummaryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AppUserMapper {
    @Mapping(target = "players", source = "players")
    AppUserDTO toDTO(AppUserEntity entity);

    List<AppUserDTO> toDtoList(List<AppUserEntity> entities);

    @Mapping(source = "nickname", target = "nickName")
    @Mapping(target = "fullName", ignore = true)
    PlayerDTO toPlayerDTO(PlayerEntity entity);
}
