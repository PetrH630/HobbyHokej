package cz.phsoft.hokej.models.services.notification;
import cz.phsoft.hokej.data.entities.MatchEntity;

import java.time.LocalDateTime;


public record MatchTimeChangeContext(
        MatchEntity match,
        LocalDateTime oldDateTime
) {}
