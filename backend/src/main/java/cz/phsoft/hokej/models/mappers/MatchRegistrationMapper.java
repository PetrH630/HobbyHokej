package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.Team;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper pro převod registrací hráčů na zápasy
 * mezi entitním a DTO modelem.
 *
 * Zajišťuje konzistentní přenos stavu registrace,
 * omluv a administrativních metadat.
 */
@Mapper(componentModel = "spring")
public interface MatchRegistrationMapper {

    /**
     * Vytvoří novou entitu registrace.
     *
     * Systémová metadata jsou nastavena
     * automaticky při mapování.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    MatchRegistrationEntity toEntity(
            MatchEntity match,
            PlayerEntity player,
            PlayerMatchStatus status,
            ExcuseReason excuseReason,
            String excuseNote,
            Team team,
            String adminNote,
            String createdBy
    );

    /**
     * Převede entitu registrace na DTO.
     *
     * @param entity entita registrace
     * @return DTO registrace
     */
    @Mapping(target = "matchId", source = "match.id")
    @Mapping(target = "playerId", source = "player.id")
    MatchRegistrationDTO toDTO(MatchRegistrationEntity entity);

    /**
     * Převede seznam registrací na seznam DTO.
     *
     * @param entities seznam entit
     * @return seznam DTO
     */
    List<MatchRegistrationDTO> toDTOList(List<MatchRegistrationEntity> entities);
}
