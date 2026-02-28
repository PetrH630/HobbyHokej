package cz.phsoft.hokej.player.mappers;

import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.dto.PlayerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Mapper pro převod mezi entitou PlayerEntity
 * a přenosovým objektem PlayerDTO.
 *
 * Slouží k oddělení databázové vrstvy od prezentační vrstvy.
 * Mapování je generováno knihovnou MapStruct na základě
 * definovaných metod tohoto rozhraní.
 *
 * Mapper je používán servisní vrstvou při vytváření,
 * aktualizaci a načítání hráčů. Některé vlastnosti
 * nejsou mapovány automaticky, protože jsou odvozené
 * nebo řízené aplikační logikou (např. fullName,
 * timestamp, vazba na uživatele).
 */
@Mapper(componentModel = "spring")
public interface PlayerMapper {

    /**
     * Převede entitu hráče na DTO.
     *
     * Odvozená vlastnost fullName není mapována,
     * protože je sestavována aplikační logikou
     * (v PlayerDTO.updateFullName / PlayerEntity.updateFullName).
     *
     * Ostatní vlastnosti (včetně primaryPosition a secondaryPosition)
     * se mapují přímo podle názvu.
     *
     * @param entity entita hráče
     * @return DTO reprezentující hráče
     */
    @Mapping(target = "fullName", ignore = true)
    PlayerDTO toDTO(PlayerEntity entity);

    /**
     * Převede DTO na novou entitu hráče.
     *
     * Vazba na uživatele není mapována a je nastavována
     * servisní vrstvou. Stav hráče je při vytvoření
     * implicitně nastaven na hodnotu PENDING.
     * Časové razítko je řízeno databází nebo aplikační logikou.
     *
     * Primární a sekundární pozice se mapují podle názvu vlastnosti.
     *
     * @param dto přenosový objekt hráče
     * @return nová entita hráče
     */
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "playerStatus", defaultValue = "PENDING")
    @Mapping(target = "timestamp", ignore = true)
    PlayerEntity toEntity(PlayerDTO dto);

    /**
     * Aktualizuje existující DTO hodnotami ze zdrojového DTO.
     *
     * Identifikátor a časové razítko zůstávají beze změny.
     *
     * @param source zdrojové DTO obsahující nové hodnoty
     * @param target cílové DTO určené k aktualizaci
     */
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    void updatePlayerDTO(PlayerDTO source, @MappingTarget PlayerDTO target);

    /**
     * Aktualizuje existující entitu hráče hodnotami z DTO.
     *
     * Vazba na uživatele a časové razítko nejsou měněny,
     * protože jsou řízeny servisní vrstvou nebo databází.
     *
     * @param source přenosový objekt obsahující nové hodnoty
     * @param target existující entita určená k aktualizaci
     */
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    void updatePlayerEntity(PlayerDTO source, @MappingTarget PlayerEntity target);

    /**
     * Převede seznam entit hráčů na seznam DTO objektů.
     *
     * @param players seznam entit hráčů
     * @return seznam DTO reprezentací hráčů
     */
    List<PlayerDTO> toDTOList(List<PlayerEntity> players);
}