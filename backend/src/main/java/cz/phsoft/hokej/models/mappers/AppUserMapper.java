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
     * k danému uživateli.
     *
     * @param entity entita uživatele
     * @return DTO reprezentace uživatele
     */
    @Mapping(target = "players", source = "players")
    AppUserDTO toDTO(AppUserEntity entity);

    /**
     * Převede seznam uživatelských entit na seznam DTO.
     *
     * @param entities seznam uživatelských entit
     * @return seznam DTO
     */
    List<AppUserDTO> toDtoList(List<AppUserEntity> entities);

    /**
     * Převede entitu hráče na DTO.
     *
     * Pole fullName se záměrně nemapuje, protože
     * je skládáno jinde (např. v DTO logice).
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
     * jsou nastavovány v servisní vrstvě.
     *
     * @param dto registrační DTO
     * @return nová entita uživatele
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "players", ignore = true)
    AppUserEntity fromRegisterDto(RegisterUserDTO dto);

    /**
     * Aktualizuje existující entitu uživatele
     * hodnotami z DTO.
     *
     * Identita, role, heslo a vazby na hráče
     * nejsou z DTO přebírány.
     *
     * @param dto    zdrojové DTO
     * @param entity cílová entita
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "players", ignore = true)
    void updateEntityFromDto(AppUserDTO dto, @MappingTarget AppUserEntity entity);
}
