package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.AppUserSettingsEntity;
import cz.phsoft.hokej.models.dto.AppUserSettingsDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AppUserSettingsMapper {

    // =========================
    // ENTITY -> DTO
    // =========================

    @Mapping(source = "playerSelectionMode", target = "playerSelectionMode")
    @Mapping(source = "globalNotificationLevel", target = "globalNotificationLevel")
    @Mapping(source = "emailDigestTime", target = "emailDigestTime")
    AppUserSettingsDTO toDTO(AppUserSettingsEntity entity);

    // =========================
    // DTO -> ENTITY (UPDATE)
    // =========================

    /**
     * Aktualizuje existující entitu hodnotami z DTO.
     *
     * Používáme @MappingTarget, aby:
     * - se entity NENAHRAZOVALA,
     * - ale pouze aktualizovala (JPA managed entity).
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "playerSelectionMode", target = "playerSelectionMode")
    @Mapping(source = "globalNotificationLevel", target = "globalNotificationLevel")
    @Mapping(source = "emailDigestTime", target = "emailDigestTime")
    void updateEntityFromDTO(AppUserSettingsDTO dto,
                             @MappingTarget AppUserSettingsEntity entity);
}
