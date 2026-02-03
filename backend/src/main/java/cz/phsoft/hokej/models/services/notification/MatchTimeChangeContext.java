package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.entities.MatchEntity;

import java.time.LocalDateTime;

/**
 * Kontext pro notifikace související se změnou termínu zápasu.
 *
 * Odpovědnost:
 * Tento kontext se používá při vytváření notifikací, které informují hráče
 * o změně času konání konkrétního zápasu. Uchovává odkaz na daný zápas
 * a původní čas zahájení, aby mohla být v notifikačních textech
 * srozumitelně popsána změna termínu.
 *
 * Vazby:
 * Kontext se používá v notifikačním subsystému při generování textů
 * emailových nebo SMS zpráv.
 *
 * @param match Zápas, jehož termín se mění.
 * @param oldDateTime Původní datum a čas konání zápasu před změnou.
 */
public record MatchTimeChangeContext(
        MatchEntity match,
        LocalDateTime oldDateTime
) {
}
