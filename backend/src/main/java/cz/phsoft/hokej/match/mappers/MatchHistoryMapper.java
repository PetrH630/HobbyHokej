package cz.phsoft.hokej.match.mappers;

import cz.phsoft.hokej.match.dto.MatchHistoryDTO;
import cz.phsoft.hokej.match.entities.MatchHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
     * Skóre se mapuje z vloženého objektu MatchScore
     * na pole scoreLight a scoreDark. Vítěz se odvozuje
     * z doménové logiky MatchScore.
     *
     * @param entity entita reprezentující historický záznam zápasu
     * @return DTO obsahující data historického záznamu
     */
    @Mapping(source = "score.light", target = "scoreLight")
    @Mapping(source = "score.dark", target = "scoreDark")
    @Mapping(
            target = "winner",
            expression = "java(entity.getScore() != null ? entity.getScore().getWinner() : null)"
    )
    @Mapping(
            target = "result",
            expression = "java(entity.getScore() != null ? entity.getScore().getResult() : null)"
    )

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