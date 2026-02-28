package cz.phsoft.hokej.user.mappers;

import cz.phsoft.hokej.user.entities.AppUserHistoryEntity;
import cz.phsoft.hokej.user.dto.AppUserHistoryDTO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper pro převod historických záznamů uživatelů
 * na jejich DTO reprezentace.
 *
 * Slouží výhradně pro čtecí účely a auditní přehledy.
 */
@Mapper(componentModel = "spring")
public interface AppUserHistoryMapper {

    AppUserHistoryDTO toDTO(AppUserHistoryEntity entity);

    List<AppUserHistoryDTO> toDTOList(
            List<AppUserHistoryEntity> entities
    );
}
