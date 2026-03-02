package cz.phsoft.hokej.match.mappers;

import cz.phsoft.hokej.match.dto.MatchDTO;
import cz.phsoft.hokej.match.entities.MatchEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper pro převod mezi entitou zápasu a DTO.
 *
 * Řídí, jakým způsobem je vazba na sezónu
 * a skóre zápasu reprezentována v API vrstvě.
 */
@Mapper(componentModel = "spring")
public interface MatchMapper {

    /**
     * Převede entitu zápasu na DTO.
     *
     * Vazba na sezónu je reprezentována
     * pouze pomocí identifikátoru.
     * Skóre se převádí na samostatná pole
     * scoreLight a scoreDark, vítěz se odvozuje
     * z doménové logiky entity.
     *
     * @param entity entita zápasu
     * @return DTO zápasu
     */
    @Mapping(source = "season.id", target = "seasonId")
    @Mapping(source = "score.light", target = "scoreLight")
    @Mapping(source = "score.dark", target = "scoreDark")
    @Mapping(target = "winner", expression = "java(entity.getWinner())")
    @Mapping(target = "result", expression = "java(entity.getResult())")
    MatchDTO toDTO(MatchEntity entity);

    /**
     * Převede DTO na novou entitu zápasu.
     *
     * Vazba na sezónu se nastavuje až
     * v servisní vrstvě.
     * Skóre se mapuje na vložený objekt MatchScore.
     *
     * @param dto DTO zápasu
     * @return entita zápasu
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
     * Aktualizuje existující entitu zápasu.
     *
     * Vazba na sezónu se při aktualizaci
     * nemění. Skóre se aktualizuje podle
     * hodnot v DTO.
     *
     * @param dto    zdrojové DTO
     * @param entity cílová entita
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