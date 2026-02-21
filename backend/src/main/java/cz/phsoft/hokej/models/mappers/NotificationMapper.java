package cz.phsoft.hokej.models.mappers;

import cz.phsoft.hokej.data.entities.NotificationEntity;
import cz.phsoft.hokej.models.dto.NotificationDTO;
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

    @Mapping(target = "category", expression = "java(entity.getType().getCategory())")
    @Mapping(target = "important", expression = "java(entity.getType().isImportant())")
    @Mapping(target = "read", expression = "java(entity.getReadAt() != null)")
    NotificationDTO toDTO(NotificationEntity entity);

    List<NotificationDTO> toDtoList(List<NotificationEntity> entities);
}