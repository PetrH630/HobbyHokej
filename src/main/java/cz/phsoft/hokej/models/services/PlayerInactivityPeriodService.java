package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerInactivityPeriodEntity;
import cz.phsoft.hokej.models.dto.PlayerInactivityPeriodDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface PlayerInactivityPeriodService {

    List<PlayerInactivityPeriodEntity> getAll();

    PlayerInactivityPeriodEntity getById(Long id);

    List<PlayerInactivityPeriodEntity> getByPlayer(Long playerId);

    PlayerInactivityPeriodEntity create(PlayerInactivityPeriodDTO dto);

    PlayerInactivityPeriodEntity update(Long id, PlayerInactivityPeriodDTO dto);

    void delete(Long id);

    public boolean isActive(PlayerEntity player, LocalDateTime dateTime);
}
