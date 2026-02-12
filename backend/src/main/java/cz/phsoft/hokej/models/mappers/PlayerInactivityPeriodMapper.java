package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerInactivityPeriodEntity;
import cz.phsoft.hokej.models.dto.PlayerInactivityPeriodDTO;
import org.mapstruct.*;

/**
 * Mapper pro převod mezi entitou PlayerInactivityPeriodEntity
 * a přenosovým objektem PlayerInactivityPeriodDTO.
 *
 * Slouží k oddělení databázové vrstvy od prezentační vrstvy.
 * Mapování je generováno knihovnou MapStruct na základě
 * definovaných metod tohoto rozhraní.
 *
 * Mapper je používán servisní vrstvou při vytváření,
 * aktualizaci a načítání období neaktivity hráče.
 * Navázání entity na konkrétního hráče je zajištěno
 * pomocí kontextového parametru.
 */
@Mapper(componentModel = "spring")
public interface PlayerInactivityPeriodMapper {

    /**
     * Převede entitu období neaktivity na DTO.
     *
     * Identifikátor hráče je převzat z navázané entity hráče.
     *
     * @param entity entita období neaktivity
     * @return DTO reprezentující období neaktivity
     */
    @Mapping(target = "playerId", source = "player.id")
    PlayerInactivityPeriodDTO toDTO(PlayerInactivityPeriodEntity entity);

    /**
     * Převede DTO na novou entitu období neaktivity.
     *
     * Identifikátor entity je ignorován, protože se vytváří
     * nový záznam. Vazba na hráče je nastavena pomocí
     * kontextového parametru.
     *
     * @param dto přenosový objekt období neaktivity
     * @param player entita hráče předaná jako kontext
     * @return nová entita období neaktivity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "player", ignore = true)
    PlayerInactivityPeriodEntity toEntity(PlayerInactivityPeriodDTO dto,
                                          @Context PlayerEntity player);

    /**
     * Aktualizuje existující entitu období neaktivity
     * hodnotami z DTO.
     *
     * Vazba na hráče se nemění a není mapována.
     *
     * @param dto přenosový objekt obsahující nové hodnoty
     * @param entity existující entita určená k aktualizaci
     */
    @Mapping(target = "player", ignore = true)
    void updateEntityFromDto(PlayerInactivityPeriodDTO dto,
                             @MappingTarget PlayerInactivityPeriodEntity entity);

    /**
     * Vytvoří novou instanci entity období neaktivity
     * a nastaví vazbu na hráče předaného v kontextu.
     *
     * Používá se při vytváření nové entity v rámci mapování,
     * aby byla zajištěna správná asociace s hráčem.
     *
     * @param dto přenosový objekt období neaktivity
     * @param player entita hráče předaná jako kontext
     * @return nově vytvořená entita s navázaným hráčem
     */
    @ObjectFactory
    default PlayerInactivityPeriodEntity createEntity(
            PlayerInactivityPeriodDTO dto,
            @Context PlayerEntity player
    ) {
        PlayerInactivityPeriodEntity entity = new PlayerInactivityPeriodEntity();
        entity.setPlayer(player);
        return entity;
    }
}
