package cz.phsoft.hokej.user.mappers;

import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.user.dto.AppUserDTO;
import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.user.dto.RegisterUserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Mapper zajišťující převod mezi uživatelskými entitami
 * a jejich DTO reprezentacemi.
 *
 * Slouží k oddělení perzistenční vrstvy od API vrstvy.
 * Controllery pracují výhradně s DTO objekty, zatímco
 * databázová vrstva používá entity.
 *
 * Mapper také zajišťuje bezpečné mapování, kdy citlivá
 * nebo systémově řízená pole nejsou přenášena z DTO
 * přímo do entit.
 */
@Mapper(componentModel = "spring")
public interface AppUserMapper {

    /**
     * Převede uživatelskou entitu na DTO.
     *
     * Součástí DTO je i seznam hráčů přiřazených
     * k danému uživateli. Metoda se používá zejména
     * v dotazovacích (read) operacích controlleru.
     *
     * @param entity entita uživatele
     * @return DTO reprezentace uživatele
     */
    @Mapping(target = "players", source = "players")
    AppUserDTO toDTO(AppUserEntity entity);

    /**
     * Převede seznam uživatelských entit na seznam DTO.
     *
     * Mapování probíhá položku po položce pomocí metody toDTO.
     *
     * @param entities seznam uživatelských entit
     * @return seznam DTO reprezentací uživatelů
     */
    List<AppUserDTO> toDtoList(List<AppUserEntity> entities);

    /**
     * Převede entitu hráče na DTO.
     *
     * Pole fullName se záměrně nemapuje, protože
     * je skládáno jinde (například v DTO logice
     * nebo ve frontendové části aplikace).
     *
     * @param entity entita hráče
     * @return DTO hráče
     */
    @Mapping(target = "fullName", ignore = true)
    PlayerDTO toPlayerDTO(PlayerEntity entity);

    /**
     * Převede registrační DTO na novou uživatelskou entitu.
     *
     * Systémová pole nejsou mapována a jejich hodnoty
     * jsou nastavovány v servisní vrstvě (například role,
     * hash hesla, aktivace účtu nebo časová razítka).
     *
     * @param dto registrační DTO
     * @return nová entita uživatele připravená k uložení
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "players", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "currentLoginAt", ignore = true)
    AppUserEntity fromRegisterDto(RegisterUserDTO dto);

    /**
     * Aktualizuje existující entitu uživatele
     * hodnotami z DTO.
     *
     * Identita, role, heslo, vazby na hráče a systémová
     * časová pole nejsou z DTO přebírány a nadále se
     * spravují výhradně na backendu v servisní vrstvě.
     *
     * @param dto    zdrojové DTO s novými hodnotami
     * @param entity cílová entita, která má být aktualizována
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "players", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "currentLoginAt", ignore = true)
    void updateEntityFromDto(AppUserDTO dto, @MappingTarget AppUserEntity entity);
}