package cz.phsoft.hokej.notifications.mappers;

import cz.phsoft.hokej.notifications.entities.NotificationEntity;
import cz.phsoft.hokej.notifications.dto.NotificationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper pro převod mezi NotificationEntity a NotificationDTO.
 *
 * Kategorie a příznak důležitosti se odvozují z NotificationType.
 * Příznak read se odvozuje podle hodnoty readAt.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(
            target = "category",
            expression = "java(entity.getType() != null ? entity.getType().getCategory() : null)"
    )
    @Mapping(
            target = "important",
            expression = "java(entity.getType() != null && entity.getType().isImportant())"
    )
    @Mapping(
            target = "read",
            expression = "java(entity.getReadAt() != null)"
    )
    @Mapping(
            target = "matchId",
            expression = "java(entity.getMatch() != null ? entity.getMatch().getId() : null)"
    )
    NotificationDTO toDTO(NotificationEntity entity);

    List<NotificationDTO> toDtoList(List<NotificationEntity> entities);
}