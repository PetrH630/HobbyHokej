package cz.phsoft.hokej.registration.mappers;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.registration.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.registration.enums.ExcuseReason;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper pro převod registrací hráčů na zápasy
 * mezi entitním a DTO modelem.
 *
 * Mapování je generováno nástrojem MapStruct.
 * Slouží k oddělení databázové vrstvy od prezentační
 * a k zajištění konzistentního přenosu stavu registrace,
 * omluv a administrativních metadat.
 */
@Mapper(componentModel = "spring")
public interface MatchRegistrationMapper {

    /**
     * Vytvoří novou entitu registrace.
     *
     * Identifikátor entity je ignorován a časové razítko
     * je nastaveno na aktuální čas při mapování.
     *
     * @param match        entita zápasu
     * @param player       entita hráče
     * @param status       stav registrace
     * @param excuseReason důvod omluvy
     * @param excuseNote   poznámka k omluvě
     * @param team         tým hráče
     * @param adminNote    administrativní poznámka
     * @param createdBy    identifikace původu registrace
     * @return nová entita registrace
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
     * Identifikátory zápasu a hráče jsou získány
     * z navázaných entit.
     *
     * @param entity entita registrace
     * @return DTO registrace
     */
    @Mapping(target = "matchId", source = "match.id")
    @Mapping(target = "playerId", source = "player.id")
    MatchRegistrationDTO toDTO(MatchRegistrationEntity entity);

    /**
     * Převede seznam entit registrací na seznam DTO.
     *
     * @param entities seznam entit
     * @return seznam DTO
     */
    List<MatchRegistrationDTO> toDTOList(List<MatchRegistrationEntity> entities);
}