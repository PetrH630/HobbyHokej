package cz.phsoft.hokej.registration.mappers;

import cz.phsoft.hokej.registration.entities.MatchRegistrationHistoryEntity;
import cz.phsoft.hokej.registration.dto.MatchRegistrationHistoryDTO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper pro převod mezi entitou MatchRegistrationHistoryEntity
 * a přenosovým objektem MatchRegistrationHistoryDTO.
 *
 * Slouží k oddělení databázové vrstvy od prezentační vrstvy.
 * Mapování je generováno knihovnou MapStruct na základě
 * definovaných metod tohoto rozhraní.
 *
 * Mapper je používán servisní vrstvou pro převod historických
 * záznamů registrací načtených z databáze do DTO objektů,
 * které jsou následně vráceny kontrolerem pro auditní
 * a přehledové účely.
 */
@Mapper(componentModel = "spring")
public interface MatchRegistrationHistoryMapper {

    /**
     * Převede entitu historického záznamu registrace na DTO.
     *
     * @param entity entita reprezentující historický záznam registrace
     * @return DTO obsahující data historického záznamu
     */
    MatchRegistrationHistoryDTO toDTO(MatchRegistrationHistoryEntity entity);

    /**
     * Převede seznam entit historických záznamů registrací na seznam DTO.
     *
     * @param entities seznam entit historických záznamů registrací
     * @return seznam DTO objektů
     */
    List<MatchRegistrationHistoryDTO> toDTOList(
            List<MatchRegistrationHistoryEntity> entities
    );
}
