package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.models.dto.MatchDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper pro převod mezi entitou zápasu a DTO.
 *
 * Řídí, jakým způsobem je vazba na sezónu
 * reprezentována v API vrstvě.
 */
@Mapper(componentModel = "spring")
public interface MatchMapper {

    /**
     * Převede entitu zápasu na DTO.
     *
     * Vazba na sezónu je reprezentována
     * pouze pomocí identifikátoru.
     *
     * @param entity entita zápasu
     * @return DTO zápasu
     */
    @Mapping(source = "season.id", target = "seasonId")
    MatchDTO toDTO(MatchEntity entity);

    /**
     * Převede DTO na novou entitu zápasu.
     *
     * Vazba na sezónu se nastavuje až
     * v servisní vrstvě.
     *
     * @param dto DTO zápasu
     * @return entita zápasu
     */
    @Mapping(target = "season", ignore = true)
    MatchEntity toEntity(MatchDTO dto);

    /**
     * Aktualizuje existující entitu zápasu.
     *
     * Vazba na sezónu se při aktualizaci
     * nemění.
     *
     * @param dto    zdrojové DTO
     * @param entity cílová entita
     */
    @Mapping(target = "season", ignore = true)
    void updateEntity(MatchDTO dto, @MappingTarget MatchEntity entity);
}
