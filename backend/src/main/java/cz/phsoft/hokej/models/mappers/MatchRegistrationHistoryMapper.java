package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.MatchRegistrationHistoryEntity;
import cz.phsoft.hokej.models.dto.MatchRegistrationHistoryDTO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper pro převod historických záznamů registrací
 * na jejich DTO reprezentace.
 *
 * Slouží výhradně pro čtecí účely a auditní přehledy.
 */
@Mapper(componentModel = "spring")
public interface MatchRegistrationHistoryMapper {

    MatchRegistrationHistoryDTO toDTO(MatchRegistrationHistoryEntity entity);

    List<MatchRegistrationHistoryDTO> toDTOList(
            List<MatchRegistrationHistoryEntity> entities
    );
}
