package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerInactivityPeriodEntity;
import cz.phsoft.hokej.models.dto.PlayerInactivityPeriodDTO;
import org.mapstruct.*;

/**
 * MapStruct mapper pro převod mezi entitou období neaktivity hráče
 * {@link PlayerInactivityPeriodEntity} a jejím DTO {@link PlayerInactivityPeriodDTO}.
 *
 * <p>
 * Zajišťuje transformaci dat mezi perzistenční vrstvou a API vrstvou
 * pro období, kdy je hráč dočasně nedostupný (zranění, dovolená, dlouhodobá absence).
 * </p>
 *
 * <h3>Architektonická role</h3>
 * <ul>
 *     <li>oddělení entitního modelu od DTO používaného v API,</li>
 *     <li>centralizované mapování období neaktivity hráče,</li>
 *     <li>zajištění konzistentního nastavení vazby na hráče pomocí {@code @Context}.</li>
 * </ul>
 *
 * <h3>Implementační poznámky</h3>
 * <ul>
 *     <li>v DTO se používá {@code playerId} místo celé {@code PlayerEntity},</li>
 *     <li>vazba na hráče je předávána přes {@link Context} a nastavuje se v {@link ObjectFactory},</li>
 *     <li>MapStruct generuje implementaci jako Spring bean.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface PlayerInactivityPeriodMapper {

    /**
     * Převede entitu období neaktivity na DTO reprezentaci.
     *
     * <p>
     * Entitní vazba na hráče ({@code player}) je v DTO reprezentována
     * pouze pomocí identifikátoru {@code playerId}. Ostatní pole se
     * mapují podle shody názvů mezi entitou a DTO.
     * </p>
     *
     * @param entity entita období neaktivity hráče
     * @return DTO reprezentace období neaktivity
     */
    @Mapping(target = "playerId", source = "player.id")
    PlayerInactivityPeriodDTO toDTO(PlayerInactivityPeriodEntity entity);

    /**
     * Převede DTO období neaktivity na novou entitu.
     *
     * <p>
     * Vazba na hráče ({@code player}) se explicitně ignoruje, protože
     * je nastavována v tovární metodě označené {@link ObjectFactory},
     * která využívá {@link PlayerEntity} předanou v {@link Context}.
     * </p>
     *
     * <p>
     * Identifikátor {@code id} je ignorován a generuje se až databází.
     * </p>
     *
     * @param dto    DTO reprezentace období neaktivity
     * @param player entita hráče předaná v kontextu mapování
     * @return nově vytvořená entita období neaktivity připravená k uložení
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "player", ignore = true)
    PlayerInactivityPeriodEntity toEntity(PlayerInactivityPeriodDTO dto, @Context PlayerEntity player);

    /**
     * Aktualizuje existující entitu období neaktivity na základě hodnot z DTO.
     *
     * <p>
     * Vazba na hráče ({@code player}) se při aktualizaci nemění a je proto
     * z mapování záměrně vyloučena. Změna přiřazeného hráče k období neaktivity
     * by měla být řešena explicitně v servisní vrstvě, pokud je vůbec povolena.
     * </p>
     *
     * @param dto    zdrojové DTO s novými hodnotami
     * @param entity cílová entita, která má být aktualizována
     */
    @Mapping(target = "player", ignore = true)
    void updateEntityFromDto(PlayerInactivityPeriodDTO dto, @MappingTarget PlayerInactivityPeriodEntity entity);

    /**
     * Tovární metoda pro vytvoření nové entity období neaktivity.
     *
     * <p>
     * Jediný správný způsob, jak nastavit vazbu na hráče při mapování DTO → entita.
     * MapStruct použije tuto metodu při vytváření nové instance
     * {@link PlayerInactivityPeriodEntity}, přičemž hráč je předán v {@link Context}.
     * </p>
     *
     * @param dto    zdrojové DTO (obsahuje data období neaktivity)
     * @param player entita hráče, ke kterému se období neaktivity vztahuje
     * @return nová entita období neaktivity s nastaveným hráčem
     */
    @ObjectFactory
    default PlayerInactivityPeriodEntity createEntity(PlayerInactivityPeriodDTO dto, @Context PlayerEntity player) {
        PlayerInactivityPeriodEntity entity = new PlayerInactivityPeriodEntity();
        entity.setPlayer(player);
        return entity;
    }
}
