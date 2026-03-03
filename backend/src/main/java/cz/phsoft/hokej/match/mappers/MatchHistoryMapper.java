package cz.phsoft.hokej.match.mappers;

import cz.phsoft.hokej.match.dto.MatchHistoryDTO;
import cz.phsoft.hokej.match.entities.MatchHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper pro převod mezi entitou MatchHistoryEntity
 * a přenosovým objektem MatchHistoryDTO.
 *
 * Mapper odděluje databázovou reprezentaci historického záznamu
 * od prezentační vrstvy. Mapování je generováno knihovnou MapStruct
 * na základě deklarovaných metod a anotací.
 *
 * Skóre je převáděno z vloženého objektu MatchScore
 * na samostatná pole DTO. Vítěz a výsledek jsou
 * odvozeny z doménové logiky objektu MatchScore.
 */
@Mapper(componentModel = "spring")
public interface MatchHistoryMapper {

    /**
     * Převede entitu historického záznamu zápasu na DTO.
     *
     * @param entity entita reprezentující historický snapshot zápasu
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
     * Převede seznam entit historických záznamů
     * na seznam DTO objektů.
     *
     * @param entities seznam entit
     * @return seznam DTO reprezentací
     */
    List<MatchHistoryDTO> toDTOList(List<MatchHistoryEntity> entities);
}