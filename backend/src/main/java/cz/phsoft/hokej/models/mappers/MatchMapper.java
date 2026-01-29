package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.models.dto.MatchDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper pro převod mezi entitou zápasu {@link MatchEntity}
 * a jejím DTO {@link MatchDTO}.
 *
 * <p>
 * Zajišťuje transformaci dat mezi perzistenční vrstvou a API vrstvou
 * tak, aby controllery a FE pracovaly s jednoduchým DTO modelem, zatímco
 * databázová reprezentace může zůstat navázaná na entitní vztahy
 * (např. vazba na sezónu).
 * </p>
 *
 * <h3>Architektonická role</h3>
 * <ul>
 *     <li>oddělení entit od DTO modelu používaného v API,</li>
 *     <li>centralizace mapování pro zápasy,</li>
 *     <li>řízení, jak se pracuje s vazbou na sezónu (ID vs. entita).</li>
 * </ul>
 *
 * <h3>Implementační poznámky</h3>
 * <ul>
 *     <li>v DTO se používá {@code seasonId} místo celé {@code SeasonEntity},</li>
 *     <li>nastavení entitní vazby na {@code season} probíhá v servisní vrstvě,</li>
 *     <li>MapStruct generuje implementaci jako Spring bean.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface MatchMapper {

    /**
     * Převede entitu zápasu na DTO reprezentaci.
     *
     * <p>
     * Entitní vazba na sezónu ({@code season}) se převádí na jednoduché
     * identifikátorové pole {@code seasonId}. Ostatní pole se mapují
     * podle shody názvů mezi entitou a DTO.
     * </p>
     *
     * @param entity entita zápasu načtená z databáze
     * @return DTO reprezentace zápasu včetně {@code seasonId}
     */
    @Mapping(source = "season.id", target = "seasonId")
    MatchDTO toDTO(MatchEntity entity);

    /**
     * Převede DTO zápasu na entitu.
     *
     * <p>
     * Pole {@code season} je z mapování záměrně vyloučeno, protože
     * nastavení vazby na konkrétní {@code SeasonEntity} probíhá až
     * v servisní vrstvě na základě {@code seasonId} z DTO.
     * </p>
     *
     * @param dto DTO reprezentace zápasu
     * @return nová entita zápasu připravená k doplnění sezóny a uložení
     */
    @Mapping(target = "season", ignore = true)
    MatchEntity toEntity(MatchDTO dto);

    /**
     * Aktualizuje existující entitu zápasu na základě hodnot z DTO.
     *
     * <p>
     * Metoda nemění vazbu na sezónu, tj. pole {@code season} je z mapování
     * záměrně ignorováno. Změna sezóny (přepojení na jinou entitu) se má
     * provádět explicitně v servisní vrstvě.
     * </p>
     *
     * @param dto    zdrojové DTO s novými hodnotami
     * @param entity cílová entita, která má být aktualizována
     */
    @Mapping(target = "season", ignore = true)
    void updateEntity(MatchDTO dto, @MappingTarget MatchEntity entity);
}
