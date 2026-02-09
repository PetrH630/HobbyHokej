package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.PlayerHistoryEntity;
import cz.phsoft.hokej.models.dto.PlayerHistoryDTO;

import java.util.List;

public interface PlayerHistoryMapper {
    PlayerHistoryDTO toDTO(PlayerHistoryEntity entity);

    List<PlayerHistoryDTO> toDTOList(
            List<PlayerHistoryEntity> entities
    );
}
