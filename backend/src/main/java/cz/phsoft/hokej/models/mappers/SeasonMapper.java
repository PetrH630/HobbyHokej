package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.SeasonEntity;
import cz.phsoft.hokej.models.dto.SeasonDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper pro převod mezi entitou sezóny {@link SeasonEntity}
 * a jejím DTO {@link SeasonDTO}.
 *
 * <p>
 * Slouží k transformaci dat mezi perzistenční vrstvou a API vrstvou
 * pro správu hokejových sezón (časové vymezení, aktivní sezóna apod.).
 * </p>
 *
 * <h3>Architektonická role</h3>
 * <ul>
 *     <li>oddělení entitního modelu sezóny od DTO používaného v API,</li>
 *     <li>centralizace mapování sezón v aplikaci,</li>
 *     <li>zajištění konzistentní práce s daty sezón napříč vrstvami.</li>
 * </ul>
 *
 * <h3>Implementační poznámky</h3>
 * <ul>
 *     <li>mapování probíhá převážně automaticky na základě shodných názvů atributů,</li>
 *     <li>žádné vazby na jiné entity se zde explicitně neřeší,</li>
 *     <li>MapStruct generuje implementaci tohoto rozhraní jako Spring bean.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface SeasonMapper {

    /**
     * Převede entitu sezóny na DTO reprezentaci.
     *
     * <p>
     * Používá se při vracení dat sezón do API (např. přehled sezón,
     * aktuální aktivní sezóna, detail sezóny).
     * </p>
     *
     * @param entity entita sezóny načtená z databáze
     * @return DTO reprezentace sezóny
     */
    SeasonDTO toDTO(SeasonEntity entity);

    /**
     * Převede DTO sezóny na novou entitu.
     *
     * <p>
     * Typicky se používá při vytváření nové sezóny. Identifikátor
     * sezóny je generován databází a není zde řešen explicitně.
     * </p>
     *
     * @param dto DTO reprezentace sezóny
     * @return nová entita sezóny připravená k uložení
     */
    SeasonEntity toEntity(SeasonDTO dto);

    /**
     * Aktualizuje existující entitu sezóny na základě hodnot z DTO.
     *
     * <p>
     * Metoda nemění identitu sezóny, pouze aktualizuje její atributy
     * (např. datumy, stav aktivace). Slouží pro editaci existující
     * sezóny v administrátorském rozhraní.
     * </p>
     *
     * @param dto    zdrojové DTO s novými hodnotami
     * @param entity cílová entita sezóny, která má být aktualizována
     */
    void updateEntityFromDTO(SeasonDTO dto, @MappingTarget SeasonEntity entity);
}
