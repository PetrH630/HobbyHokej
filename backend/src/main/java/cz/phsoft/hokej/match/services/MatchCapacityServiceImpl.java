package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.registration.services.MatchRegistrationCommandService;
import cz.phsoft.hokej.registration.services.MatchRegistrationService;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementace service vrstvy pro přepočet kapacity zápasu.
 *
 * Třída centralizuje logiku, která se spouští při změně parametru maxPlayers:
 * - při snížení kapacity přepočítává stavy REGISTERED a RESERVED tak,
 *   aby odpovídaly nové kapacitě,
 * - při navýšení kapacity vybírá vhodné kandidáty ze stavu RESERVED a
 *   povyšuje je do stavu REGISTERED, přičemž nová místa rozděluje mezi
 *   týmy DARK a LIGHT.
 *
 * Pro čtení registrací se používá {@link MatchRegistrationService}.
 * Pro změny stavů registrací se používá {@link MatchRegistrationCommandService}.
 */
@Service
public class MatchCapacityServiceImpl implements MatchCapacityService {

    private final MatchRegistrationService registrationService;
    private final MatchRegistrationCommandService registrationCommandService;

    public MatchCapacityServiceImpl(
            MatchRegistrationService registrationService,
            MatchRegistrationCommandService registrationCommandService
    ) {
        this.registrationService = registrationService;
        this.registrationCommandService = registrationCommandService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void handleCapacityChange(MatchEntity match, Integer oldMaxPlayers) {
        if (match == null) {
            return;
        }

        Integer newMaxPlayers = match.getMaxPlayers();
        if (newMaxPlayers == null || oldMaxPlayers == null) {
            return;
        }

        int diffMaxPlayers = newMaxPlayers - oldMaxPlayers;
        if (diffMaxPlayers == 0) {
            return;
        }

        // SNÍŽENÍ kapacity – přebyteční hráči se přesunou do stavu RESERVED
        if (diffMaxPlayers < 0) {
            registrationCommandService.recalcStatusesForMatch(match.getId());
            return;
        }

        // ZVÝŠENÍ kapacity – navýšená místa se rozdělí mezi týmy
        handleCapacityIncrease(match, diffMaxPlayers);
    }

    /**
     * Zpracovává navýšení kapacity zápasu.
     *
     * Nová místa se rozdělí mezi týmy DARK a LIGHT tak, aby se co nejvíce
     * vyrovnal počet registrovaných hráčů v obou týmech. Při lichém počtu
     * nových míst dostává extra slot tým, který má aktuálně méně hráčů
     * ve stavu REGISTERED.
     *
     * Povyšování kandidátů ze stavu RESERVED probíhá přes
     * {@link MatchRegistrationCommandService#promoteReservedCandidatesForCapacityIncrease(Long, Team, PlayerPosition, int)}.
     *
     * @param match          Zápas, u kterého byla navýšena kapacita.
     * @param totalNewSlots  Celkový počet nových míst (rozdíl mezi novou a původní kapacitou).
     */
    private void handleCapacityIncrease(MatchEntity match, int totalNewSlots) {
        if (totalNewSlots <= 0) {
            return;
        }

        Long matchId = match.getId();

        // Registrace k danému zápasu – stačí DTO z čtecí service
        List<MatchRegistrationDTO> regsForSlots =
                registrationService.getRegistrationsForMatch(matchId);

        int registeredDark = (int) regsForSlots.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .filter(r -> r.getTeam() == Team.DARK)
                .count();

        int registeredLight = (int) regsForSlots.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .filter(r -> r.getTeam() == Team.LIGHT)
                .count();

        // Základ – rovnoměrné rozdělení nových míst
        int baseSlotsPerTeam = totalNewSlots / 2;
        int extraSlot = totalNewSlots % 2;

        int darkSlots = baseSlotsPerTeam;
        int lightSlots = baseSlotsPerTeam;

        // Liché extra místo dostane tým, který má aktuálně méně registrovaných hráčů
        if (extraSlot > 0) {
            if (registeredDark <= registeredLight) {
                darkSlots += 1;
            } else {
                lightSlots += 1;
            }
        }

        if (darkSlots > 0) {
            registrationCommandService.promoteReservedCandidatesForCapacityIncrease(
                    matchId,
                    Team.DARK,   // preferovaný tým pro nová místa
                    null,        // pozice se neomezuje – rozhoduje logika kandidáta
                    darkSlots
            );
        }

        if (lightSlots > 0) {
            registrationCommandService.promoteReservedCandidatesForCapacityIncrease(
                    matchId,
                    Team.LIGHT,
                    null,
                    lightSlots
            );
        }
    }
}