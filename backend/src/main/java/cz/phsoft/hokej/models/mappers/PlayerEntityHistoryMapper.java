package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.PlayerEntityHistory;
import cz.phsoft.hokej.models.dto.PlayerHistoryDTO;

import java.util.List;

public interface PlayerEntityHistoryMapper {
    PlayerHistoryDTO toDTO(PlayerEntityHistory entity);

    List<PlayerHistoryDTO> toDTOList(
            List<PlayerEntityHistory> entities
    );
}
