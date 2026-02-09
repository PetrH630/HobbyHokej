package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.MatchHistoryEntity;
import cz.phsoft.hokej.models.dto.MatchHistoryDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MatchHistoryMapper {
    MatchHistoryDTO toDTO(MatchHistoryEntity entity);

    List<MatchHistoryDTO> toDTOList(
            List<MatchHistoryEntity> entities
    );
}
