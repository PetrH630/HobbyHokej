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
 * Mapování je generováno knihovnou MapStruct na základě
 * definovaných metod tohoto rozhraní.
 *
 * Mapper je používán servisní vrstvou pro převod historických
 * záznamů hráče načtených z databáze do DTO objektů,
 * které jsou následně vráceny kontrolerem pro auditní
 * a přehledové účely.
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
    List<PlayerHistoryDTO> toDTOList(
            List<PlayerHistoryEntity> entities
    );
}
