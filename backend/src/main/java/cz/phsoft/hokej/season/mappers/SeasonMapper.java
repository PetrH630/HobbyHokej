package cz.phsoft.hokej.season.mappers;

import cz.phsoft.hokej.season.entities.SeasonEntity;
import cz.phsoft.hokej.season.dto.SeasonDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper pro převod mezi entitou SeasonEntity
 * a přenosovým objektem SeasonDTO.
 *
 * Slouží k oddělení databázové vrstvy od prezentační vrstvy.
 * Mapování je generováno knihovnou MapStruct na základě
 * definovaných metod tohoto rozhraní.
 *
 * Mapper je používán servisní vrstvou při vytváření,
 * aktualizaci a načítání sezón. Některé vlastnosti
 * nejsou mapovány automaticky, protože jsou řízeny
 * aplikační logikou nebo databází.
 */
@Mapper(componentModel = "spring")
public interface SeasonMapper {

    /**
     * Převede entitu sezóny na DTO.
     *
     * @param entity entita sezóny
     * @return DTO reprezentující sezónu
     */
    SeasonDTO toDTO(SeasonEntity entity);

    /**
     * Převede DTO na novou entitu sezóny.
     *
     * Identifikátor, časové razítko a informace o uživateli,
     * který sezónu vytvořil, nejsou mapovány, protože jsou
     * nastavovány servisní vrstvou nebo databází.
     *
     * @param dto přenosový objekt sezóny
     * @return nová entita sezóny
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    SeasonEntity toEntity(SeasonDTO dto);

    /**
     * Aktualizuje existující entitu sezóny hodnotami z DTO.
     *
     * Identifikátor, časové razítko a informace o uživateli,
     * který sezónu vytvořil, zůstávají beze změny.
     *
     * @param dto přenosový objekt obsahující nové hodnoty
     * @param entity existující entita určená k aktualizaci
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    void updateEntityFromDTO(SeasonDTO dto,
                             @MappingTarget SeasonEntity entity);
}
