package cz.phsoft.hokej.player.mappers;

import cz.phsoft.hokej.player.entities.PlayerHistoryEntity;
import cz.phsoft.hokej.player.dto.PlayerHistoryDTO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper pro převod mezi entitou PlayerHistoryEntity
 * a přenosovým objektem PlayerHistoryDTO.
 *
 * Slouží k oddělení databázové vrstvy od prezentační vrstvy.
 * Implementace mapování je generována knihovnou MapStruct
 * na základě definovaných metod tohoto rozhraní.
 *
 * Mapper je používán servisní vrstvou při načítání
 * historických záznamů hráče z databáze a jejich převodu
 * na DTO objekty vracené controllerem.
 */
@Mapper(componentModel = "spring")
public interface PlayerHistoryMapper {

    /**
     * Převede entitu historického záznamu hráče na DTO.
     *
     * @param entity entita reprezentující historický záznam hráče
     * @return DTO obsahující data historického záznamu
     */
    PlayerHistoryDTO toDTO(PlayerHistoryEntity entity);

    /**
     * Převede seznam entit historických záznamů hráče na seznam DTO.
     *
     * @param entities seznam entit historických záznamů hráče
     * @return seznam DTO objektů
     */
    List<PlayerHistoryDTO> toDTOList(List<PlayerHistoryEntity> entities);
}