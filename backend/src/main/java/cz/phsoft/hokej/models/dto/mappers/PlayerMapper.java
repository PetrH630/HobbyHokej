package cz.phsoft.hokej.models.dto.mappers;

import cz.phsoft.hokej.data.entities.NotificationSettings;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct mapper pro převod mezi entitou hráče {@link PlayerEntity}
 * a jejím DTO {@link PlayerDTO}.
 *
 * <p>
 * Zajišťuje transformaci dat mezi perzistenční vrstvou a API vrstvou,
 * včetně mapování notifikačních nastavení a statusu hráče.
 * </p>
 *
 * <h3>Architektonická role</h3>
 * <ul>
 *     <li>oddělení entitního modelu hráče od DTO používaného v API,</li>
 *     <li>centralizace mapování hráčských objektů,</li>
 *     <li>řízení mapování vloženého objektu {@link NotificationSettings}.</li>
 * </ul>
 *
 * <h3>Implementační poznámky</h3>
 * <ul>
 *     <li>pole {@code fullName} se negeneruje v mapperu, ale v entitě/DTO logice,</li>
 *     <li>vazba na uživatele ({@code user}) se nastavuje výhradně v servisní vrstvě,</li>
 *     <li>notifikační nastavení se mapují do embedded objektu {@link NotificationSettings}.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface PlayerMapper {

    /**
     * Převede entitu hráče na DTO reprezentaci.
     *
     * <p>
     * Rozdílné názvy atributů jsou mapovány explicitně
     * Celé jméno hráče
     * ({@code fullName}) se v této fázi neplní a předpokládá se jeho
     * sestavení jinde (např. z křestního jména a příjmení).
     * </p>
     *
     * <p>
     * Notifikační nastavení se rozbalují z embedded objektu
     * {@link NotificationSettings} do jednoduchých boolean příznaků
     * {@code notifyByEmail} a {@code notifyBySms}.
     * </p>
     *
     * @param entity entita hráče načtená z databáze
     * @return DTO reprezentace hráče
     */
    @Mapping(target = "fullName", ignore = true)
    @Mapping(source = "notificationSettings.emailEnabled", target = "notifyByEmail")
    @Mapping(source = "notificationSettings.smsEnabled", target = "notifyBySms")
    PlayerDTO toDTO(PlayerEntity entity);

    /**
     * Převede DTO hráče na novou entitu.
     *
     <p>
     * Vazba na uživatele ({@code user}) se nemapuje – je plně spravována
     * servisní vrstvou. Status hráče se nastavuje podle hodnoty v DTO;
     * pokud není vyplněn, použije se výchozí hodnota {@code PENDING}.
     * </p>
     *
     * <p>
     * Notifikační příznaky ({@code notifyByEmail}, {@code notifyBySms})
     * se mapují do embedded objektu {@link NotificationSettings}.
     * </p>
     *
     * @param dto DTO reprezentace hráče
     * @return nová entita hráče připravená k uložení
     */
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(
            target = "playerStatus",
            expression = "java(dto.getPlayerStatus() != null ? dto.getPlayerStatus() : cz.phsoft.hokej.data.enums.PlayerStatus.PENDING)"
    )
    @Mapping(target = "notificationSettings.emailEnabled", source = "notifyByEmail")
    @Mapping(target = "notificationSettings.smsEnabled", source = "notifyBySms")
    PlayerEntity toEntity(PlayerDTO dto);

    /**
     * Aktualizuje existující DTO hráče z jiného DTO.
     *
     * <p>
     * Používá se v situacích, kdy je potřeba aktualizovat DTO objekt
     * (např. v rámci FE/API vrstvy) bez změny identifikátoru a
     * bez zásahu do pole {@code fullName}, které se sestavuje jinde.
     * </p>
     *
     * @param source zdrojové DTO s novými hodnotami
     * @param target cílové DTO, které má být aktualizováno
     */
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updatePlayerDTO(PlayerDTO source, @MappingTarget PlayerDTO target);

    /**
     * Aktualizuje existující entitu hráče na základě hodnot z DTO.
     *
     * <p>
     * Celé jméno hráče ({@code fullName}) se
     * ignoruje, stejně tak vazba na uživatele ({@code user}), která je
     * spravována servisní vrstvou.
     * </p>
     *
     * <p>
     * Status hráče se aktualizuje pouze v případě, že je v DTO vyplněn.
     * Pokud je v DTO {@code null}, ponechá se stávající hodnota v entitě.
     * </p>
     *
     * <p>
     * Notifikační příznaky se opět mapují do embedded objektu
     * {@link NotificationSettings}.
     * </p>
     *
     * @param source zdrojové DTO s novými hodnotami
     * @param target cílová entita hráče, která má být aktualizována
     */
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(
            target = "playerStatus",
            expression = "java(source.getPlayerStatus() != null ? source.getPlayerStatus() : target.getPlayerStatus())"
    )
    @Mapping(target = "notificationSettings.emailEnabled", source = "notifyByEmail")
    @Mapping(target = "notificationSettings.smsEnabled", source = "notifyBySms")
    void updatePlayerEntity(PlayerDTO source, @MappingTarget PlayerEntity target);

    /**
     * Převede seznam entit hráčů na seznam DTO.
     *
     * <p>
     * Používá se typicky při vracení seznamu hráčů pro administraci
     * nebo pro přehledy. Mapování jednotlivých prvků využívá metodu
     * {@link #toDTO(PlayerEntity)}.
     * </p>
     *
     * @param players seznam entit hráčů
     * @return seznam DTO reprezentací hráčů
     */
    List<PlayerDTO> toDTOList(List<PlayerEntity> players);

    /**
     * Zajistí inicializaci notifikačního nastavení po mapování.
     *
     * <p>
     * Pokud během mapování nevznikne instance {@link NotificationSettings},
     * vytvoří se výchozí objekt s vypnutými notifikacemi. Tím se eliminuje
     * nutnost kontrolovat {@code null} v dalších částech aplikace.
     * </p>
     *
     * @param entity cílová entita hráče po dokončení mapování
     */
    @AfterMapping
    default void ensureNotificationSettings(@MappingTarget PlayerEntity entity) {
        if (entity.getNotificationSettings() == null) {
            NotificationSettings ns = new NotificationSettings();
            ns.setEmailEnabled(false);
            ns.setSmsEnabled(false);
            entity.setNotificationSettings(ns);
        }
    }

}
