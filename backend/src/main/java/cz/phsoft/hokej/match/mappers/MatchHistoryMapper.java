package cz.phsoft.hokej.match.mappers;

import cz.phsoft.hokej.match.entities.MatchHistoryEntity;
import cz.phsoft.hokej.match.dto.MatchHistoryDTO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper pro převod mezi entitou MatchHistoryEntity
 * a přenosovým objektem MatchHistoryDTO.
 *
 * Slouží k oddělení databázové vrstvy od prezentační vrstvy.
 * Mapování je generováno knihovnou MapStruct na základě
 * definovaných metod tohoto rozhraní.
 *
 * Mapper je používán servisní vrstvou pro převod historických
 * záznamů zápasu načtených z databáze do DTO objektů,
 * které jsou následně vráceny kontrolerem.
 */
@Mapper(componentModel = "spring")
public interface MatchHistoryMapper {

    /**
     * Převede entitu historického záznamu zápasu na DTO.
     *
     * @param entity entita reprezentující historický záznam zápasu
     * @return DTO obsahující data historického záznamu
     */
    MatchHistoryDTO toDTO(MatchHistoryEntity entity);

    /**
     * Převede seznam entit historických záznamů zápasu na seznam DTO.
     *
     * @param entities seznam entit historických záznamů
     * @return seznam DTO objektů
     */
    List<MatchHistoryDTO> toDTOList(
            List<MatchHistoryEntity> entities
    );
}
