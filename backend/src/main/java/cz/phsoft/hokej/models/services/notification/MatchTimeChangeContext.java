package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.entities.MatchEntity;

import java.time.LocalDateTime;

/**
 * Kontext pro notifikace související se změnou termínu zápasu.
 *
 * Obsahuje odkaz na zápas a původní datum a čas. Umožňuje v notifikacích
 * srozumitelně informovat hráče o nové době konání a porovnat ji
 * s původním termínem.
 */
public record MatchTimeChangeContext(
        MatchEntity match,
        LocalDateTime oldDateTime
) {
}
