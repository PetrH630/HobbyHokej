package cz.phsoft.hokej.player.mappers;

import cz.phsoft.hokej.player.entities.PlayerSettingsEntity;
import cz.phsoft.hokej.player.dto.PlayerSettingsDTO;
import org.mapstruct.*;

/**
 * Mapper pro převod mezi entitou PlayerSettingsEntity
 * a přenosovým objektem PlayerSettingsDTO.
 *
 * Slouží k oddělení databázové vrstvy od prezentační vrstvy.
 * Mapování je generováno knihovnou MapStruct na základě
 * definovaných metod tohoto rozhraní.
 *
 * Mapper je používán servisní vrstvou při načítání
 * a aktualizaci hráčských nastavení. Při aktualizaci
 * jsou null hodnoty ignorovány, aby nedošlo k nechtěnému
 * přepsání existujících dat.
 */
@Mapper(componentModel = "spring")
public interface PlayerSettingsMapper {

    /**
     * Převede entitu hráčských nastavení na DTO.
     *
     * @param entity entita nastavení hráče
     * @return DTO reprezentující nastavení hráče
     */
    PlayerSettingsDTO toDTO(PlayerSettingsEntity entity);

    /**
     * Aktualizuje existující entitu hráčských nastavení
     * hodnotami z DTO.
     *
     * Null hodnoty ve zdrojovém DTO nejsou mapovány,
     * aby zůstaly zachovány původní hodnoty v entitě.
     *
     * @param dto přenosový objekt obsahující nové hodnoty
     * @param entity existující entita určená k aktualizaci
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(PlayerSettingsDTO dto,
                             @MappingTarget PlayerSettingsEntity entity);
}
