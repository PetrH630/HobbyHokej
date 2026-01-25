package cz.phsoft.hokej.models.dto.mappers;

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
 * MapStruct mapper pro převod mezi entitou registrace na zápas
 * {@link MatchRegistrationEntity} a jejím DTO {@link MatchRegistrationDTO}.
 *
 * <p>
 * Zajišťuje transformaci dat mezi perzistenční vrstvou a API vrstvou
 * pro registrace hráčů na konkrétní zápasy včetně:
 * </p>
 * <ul>
 *     <li>stavu registrace (přihlášen, odhlášen, omluven apod.),</li>
 *     <li>týmu, do kterého je hráč přiřazen,</li>
 *     <li>omluv (důvod, poznámka),</li>
 *     <li>administrativních poznámek a metadat (createdBy, timestamp).</li>
 * </ul>
 *
 * <h3>Architektonická role</h3>
 * <ul>
 *     <li>oddělení entitního modelu registrace od DTO používaného v API,</li>
 *     <li>centralizace mapování registrací pro business a prezentační vrstvu,</li>
 *     <li>zajištění konzistentního nastavení metadat při vytváření registrace.</li>
 * </ul>
 *
 * <h3>Implementační poznámky</h3>
 * <ul>
 *     <li>při vytváření nové registrace se nastavuje timestamp na aktuální čas,</li>
 *     <li>v DTO se pro vazby používají identifikátory ({@code matchId}, {@code playerId}),</li>
 *     <li>MapStruct generuje implementaci jako Spring bean.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface MatchRegistrationMapper {

    /**
     * Vytvoří novou entitu registrace hráče na zápas.
     *
     * <p>
     * Metoda slouží jako tovární mapovací metoda pro vznik nové registrace.
     * Přijímá explicitně všechny relevantní hodnoty (zápas, hráč, stav,
     * důvod omluvy, tým, poznámky, autora změny) a doplní systémové
     * metadata (čas vytvoření).
     * </p>
     *
     * <p>
     * Identifikátor {@code id} je ignorován a generuje se až databází.
     * </p>
     *
     * @param match        entita zápasu, ke kterému se hráč registruje
     * @param player       entita hráče, který se registruje
     * @param status       stav registrace (např. REGISTERED, UNREGISTERED, EXCUSED)
     * @param excuseReason důvod omluvy (pokud je použit)
     * @param excuseNote   textová poznámka k omluvě
     * @param team         tým, do kterého je hráč přiřazen
     * @param adminNote    interní poznámka administrátora
     * @param createdBy    identifikace uživatele, který registraci vytvořil / změnil
     * @return nově vytvořená entita registrace připravená k uložení
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "match", source = "match")
    @Mapping(target = "player", source = "player")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "excuseReason", source = "excuseReason")
    @Mapping(target = "excuseNote", source = "excuseNote")
    @Mapping(target = "team", source = "team")
    @Mapping(target = "adminNote", source = "adminNote")
    @Mapping(target = "createdBy", source = "createdBy")
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
     * Převede entitu registrace na DTO reprezentaci.
     *
     * <p>
     * Entitní vazby na zápas a hráče ({@code match}, {@code player})
     * jsou v DTO reprezentovány pouze pomocí identifikátorů
     * {@code matchId} a {@code playerId}. Ostatní pole se mapují
     * podle shody názvů mezi entitou a DTO.
     * </p>
     *
     * @param entity entita registrace hráče na zápas
     * @return DTO reprezentace registrace
     */
    @Mapping(target = "matchId", source = "match.id")
    @Mapping(target = "playerId", source = "player.id")
    MatchRegistrationDTO toDTO(MatchRegistrationEntity entity);

    /**
     * Převede seznam entit registrací na seznam DTO.
     *
     * <p>
     * Používá se typicky při vracení seznamu registrací pro daný zápas
     * nebo pro daného hráče. Mapování jednotlivých prvků využívá metodu
     * {@link #toDTO(MatchRegistrationEntity)}.
     * </p>
     *
     * @param entities seznam entit registrací
     * @return seznam DTO reprezentací registrací
     */
    List<MatchRegistrationDTO> toDTOList(List<MatchRegistrationEntity> entities);
}
