package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.models.dto.AppUserDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.RegisterUserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct mapper pro převod mezi AppUser / Player entitami a jejich DTO objekty.
 *
 * <p>
 * Zajišťuje:
 * </p>
 * <ul>
 *     <li>mapování uživatelských entit na DTO pro API vrstvy,</li>
 *     <li>mapování registračních DTO na nové uživatelské entity,</li>
 *     <li>bezpečnou aktualizaci uživatelů z AppUserDTO.</li>
 * </ul>
 *
 * <h3>Architektonická role</h3>
 * <ul>
 *     <li>oddělení mapování (transformace dat) od business logiky,</li>
 *     <li>centralizované mapování uživatelských a hráčských objektů,</li>
 *     <li>jasná kontrola nad tím, která pole se přenáší a která jsou řízena pouze službami.</li>
 * </ul>
 *
 * <h3>Implementační poznámky</h3>
 * <ul>
 *     <li>cílem je pracovat s DTO v controller vrstvách a entitami v persistence vrstvě,</li>
 *     <li>bezpečnostně citlivá pole (heslo, role, stav účtu) jsou z mapování záměrně vyloučena,</li>
 *     <li>MapStruct generuje implementaci tohoto rozhraní jako Spring bean.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface AppUserMapper {

    /**
     * Převede entitu uživatele na DTO reprezentaci.
     *
     * <p>
     * Metoda slouží k přípravě dat pro API odpovědi. Součástí DTO je
     * i kolekce hráčů přiřazených k danému uživateli.
     * </p>
     *
     * @param entity uživatelská entita načtená z databáze
     * @return DTO reprezentace uživatele včetně seznamu hráčů
     */
    @Mapping(target = "players", source = "players")
    AppUserDTO toDTO(AppUserEntity entity);

    /**
     * Převede seznam uživatelských entit na seznam DTO.
     *
     * <p>
     * Typicky použito v administrátorských přehledech uživatelů.
     * Mapování jednotlivých prvků využívá metodu {@link #toDTO(AppUserEntity)}.
     * </p>
     *
     * @param entities seznam uživatelských entit
     * @return seznam uživatelských DTO
     */
    List<AppUserDTO> toDtoList(List<AppUserEntity> entities);

    /**
     * Převede entitu hráče na {@link PlayerDTO}.
     *
     * <p>
     * ignoruje pole {@code fullName}, které se typicky sestavuje jinde
     * (například v doménové logice nebo přímo v DTO).
     * </p>
     *
     * @param entity hráčská entita
     * @return DTO reprezentace hráče
     */
    @Mapping(target = "fullName", ignore = true)
    PlayerDTO toPlayerDTO(PlayerEntity entity);

    /**
     * Převede registrační DTO na novou entitu uživatele.
     *
     * <p>
     * Používá se při vytváření nového uživatelského účtu. Systémově
     * řízená pole nejsou mapována a jejich hodnota je nastavována až
     * v servisní vrstvě (např. role, heslo, stav účtu).
     * </p>
     *
     * Ignorovaná pole:
     * <ul>
     *     <li>{@code id} – generuje databáze,</li>
     *     <li>{@code password} – nastavuje se v service po zahashování,</li>
     *     <li>{@code role} – nastavuje business logika (např. výchozí role),</li>
     *     <li>{@code enabled} – stav aktivace účtu, řeší se v service,</li>
     *     <li>{@code players} – vazba na hráče se řeší samostatně.</li>
     * </ul>
     *
     * @param dto registrační DTO s údaji nového uživatele
     * @return nová entita uživatele připravená k uložení
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "players", ignore = true)
    AppUserEntity fromRegisterDto(RegisterUserDTO dto);

    /**
     * Aktualizuje existující entitu uživatele na základě hodnot z DTO.
     *
     * <p>
     * Slouží pro úpravu profilu uživatele. Metoda nevrací novou instanci,
     * ale mění stav předané entity označené jako {@link MappingTarget}.
     * </p>
     *
     * <p>
     * Kritická pole nejsou z DTO přebírána a zůstávají plně v režii
     * servisní vrstvy (např. změna role, hesla nebo stavu účtu).
     * </p>
     *
     * Ignorovaná pole:
     * <ul>
     *     <li>{@code id} – primární klíč se nemění,</li>
     *     <li>{@code password} – mění se pouze přes dedikovanou logiku změny hesla,</li>
     *     <li>{@code role} – spravuje administrace / business logika,</li>
     *     <li>{@code enabled} – stav účtu (aktivace/blokace),</li>
     *     <li>{@code players} – vazby na hráče jsou spravovány samostatně.</li>
     * </ul>
     *
     * @param dto    zdrojové DTO s novými hodnotami
     * @param entity cílová entita, která má být aktualizována
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "players", ignore = true)
    void updateEntityFromDto(AppUserDTO dto, @MappingTarget AppUserEntity entity);
}
