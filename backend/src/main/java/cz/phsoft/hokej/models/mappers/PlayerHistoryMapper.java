package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.PlayerHistoryEntity;
import cz.phsoft.hokej.models.dto.PlayerHistoryDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlayerHistoryMapper {
    PlayerHistoryDTO toDTO(PlayerHistoryEntity entity);

    List<PlayerHistoryDTO> toDTOList(
            List<PlayerHistoryEntity> entities
    );
}
