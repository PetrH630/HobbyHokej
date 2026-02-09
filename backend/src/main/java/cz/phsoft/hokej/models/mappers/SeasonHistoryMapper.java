package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.SeasonHistoryEntity;
import cz.phsoft.hokej.models.dto.SeasonHistoryDTO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper pro převod historických záznamů sezón.
 */
@Mapper(componentModel = "spring")
public interface SeasonHistoryMapper {

    SeasonHistoryDTO toDTO(SeasonHistoryEntity entity);

    List<SeasonHistoryDTO> toDTOList(List<SeasonHistoryEntity> entities);
}
