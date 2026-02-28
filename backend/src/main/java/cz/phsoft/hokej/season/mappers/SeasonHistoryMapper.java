package cz.phsoft.hokej.season.mappers;

import cz.phsoft.hokej.season.entities.SeasonHistoryEntity;
import cz.phsoft.hokej.season.dto.SeasonHistoryDTO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper pro převod mezi entitou SeasonHistoryEntity
 * a přenosovým objektem SeasonHistoryDTO.
 *
 * Slouží k oddělení databázové vrstvy od prezentační vrstvy.
 * Mapování je generováno knihovnou MapStruct na základě
 * definovaných metod tohoto rozhraní.
 *
 * Mapper je používán servisní vrstvou pro převod
 * historických záznamů sezóny načtených z databáze
 * do DTO objektů, které jsou následně vráceny kontrolerem
 * pro auditní a přehledové účely.
 */
@Mapper(componentModel = "spring")
public interface SeasonHistoryMapper {

    /**
     * Převede entitu historického záznamu sezóny na DTO.
     *
     * @param entity entita reprezentující historický záznam sezóny
     * @return DTO obsahující data historického záznamu
     */
    SeasonHistoryDTO toDTO(SeasonHistoryEntity entity);

    /**
     * Převede seznam entit historických záznamů sezóny na seznam DTO.
     *
     * @param entities seznam entit historických záznamů sezóny
     * @return seznam DTO objektů
     */
    List<SeasonHistoryDTO> toDTOList(List<SeasonHistoryEntity> entities);
}
