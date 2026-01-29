package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.MatchRegistrationHistoryEntity;
import cz.phsoft.hokej.models.dto.MatchRegistrationHistoryDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MatchRegistrationHistoryMapper {

    MatchRegistrationHistoryDTO toDTO(MatchRegistrationHistoryEntity entity);

    List<MatchRegistrationHistoryDTO> toDTOList(
            List<MatchRegistrationHistoryEntity> entities
    );
}
