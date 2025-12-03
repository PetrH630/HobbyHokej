package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;

import java.time.LocalDateTime;

public interface PlayerInactivityService {
    public boolean isActive(PlayerEntity player, LocalDateTime dateTime);
}
