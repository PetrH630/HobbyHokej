package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.PlayerInactivityPeriodRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PlayerInactivityServiceImpl implements PlayerInactivityService {

    private final PlayerInactivityPeriodRepository inactivityRepository;

    public PlayerInactivityServiceImpl(PlayerInactivityPeriodRepository inactivityRepository) {
        this.inactivityRepository = inactivityRepository;
    }

    // true = aktivní, false = neaktivní
    public boolean isActive(PlayerEntity player, LocalDateTime dateTime) {
        return !inactivityRepository.existsByPlayerAndInactiveFromLessThanEqualAndInactiveToGreaterThanEqual(
                player, dateTime, dateTime);
    }
}
