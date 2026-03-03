package cz.phsoft.hokej.match.mappers;

import cz.phsoft.hokej.match.dto.MatchDTO;
import cz.phsoft.hokej.match.entities.MatchEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper pro převod mezi entitou MatchEntity
 * a přenosovým objektem MatchDTO.
 *
 * Mapper definuje způsob reprezentace vazby na sezónu
 * a způsob převodu vloženého objektu MatchScore
 * na samostatná pole DTO.
 *
 * Mapování je generováno automaticky nástrojem MapStruct.
 */
@Mapper(componentModel = "spring")
public interface MatchMapper {

    /**
     * Převede entitu zápasu na DTO.
     *
     * Vazba na sezónu je reprezentována pouze pomocí identifikátoru.
     * Skóre je převáděno na samostatná pole scoreLight a scoreDark.
     * Vítěz a výsledek jsou odvozeny z doménové logiky entity.
     *
     * @param entity entita zápasu
     * @return DTO reprezentující zápas
     */
    @Mapping(source = "season.id", target = "seasonId")
    @Mapping(source = "score.light", target = "scoreLight")
    @Mapping(source = "score.dark", target = "scoreDark")
    @Mapping(target = "winner", expression = "java(entity.getWinner())")
    @Mapping(target = "result", expression = "java(entity.getResult())")
    MatchDTO toDTO(MatchEntity entity);

    /**
     * Vytvoří novou entitu zápasu z DTO.
     *
     * Vazba na sezónu se nenastavuje zde,
     * ale až v servisní vrstvě.
     * Auditní pole a timestamp nejsou při mapování nastavovány.
     *
     * Skóre je mapováno do vloženého objektu MatchScore.
     *
     * @param dto zdrojový DTO objekt
     * @return nová entita zápasu
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "season", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    @Mapping(target = "lastModifiedByUserId", ignore = true)
    @Mapping(target = "score.light", source = "scoreLight")
    @Mapping(target = "score.dark", source = "scoreDark")
    MatchEntity toEntity(MatchDTO dto);

    /**
     * Aktualizuje existující entitu zápasu podle hodnot v DTO.
     *
     * Identifikátor, vazba na sezónu a auditní pole
     * se při aktualizaci nemění.
     * Skóre je aktualizováno podle hodnot v DTO.
     *
     * @param dto zdrojové DTO
     * @param entity cílová entita, která bude aktualizována
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "season", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    @Mapping(target = "lastModifiedByUserId", ignore = true)
    @Mapping(target = "score.light", source = "scoreLight")
    @Mapping(target = "score.dark", source = "scoreDark")
    void updateEntity(MatchDTO dto, @MappingTarget MatchEntity entity);
}