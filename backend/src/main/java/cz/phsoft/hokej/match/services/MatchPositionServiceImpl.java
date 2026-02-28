package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.match.util.MatchModeLayoutUtil;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.match.repositories.MatchRepository;
import cz.phsoft.hokej.match.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.match.dto.MatchPositionOverviewDTO;
import cz.phsoft.hokej.match.dto.MatchPositionSlotDTO;
import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.registration.dto.MatchTeamPositionOverviewDTO;
import cz.phsoft.hokej.registration.dto.MatchTeamPositionSlotDTO;
import cz.phsoft.hokej.registration.services.MatchRegistrationService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementace service vrstvy pro přehled obsazenosti pozic v zápase.
 *
 * Tato třída kombinuje informace o konfiguraci zápasu (MatchMode, maxPlayers)
 * s aktuálními registracemi hráčů a poskytuje přehled kapacity a obsazenosti
 * pozic na ledě pro oba týmy nebo pro konkrétní tým.
 *
 * Pro čtení registrací se používá {@link MatchRegistrationService}.
 * Pro výpočet kapacity pozic pro daný MatchMode se používá
 * {@link MatchModeLayoutUtil}.
 */
@Service
public class MatchPositionServiceImpl implements MatchPositionService {

    private final MatchRepository matchRepository;
    private final MatchRegistrationService registrationService;

    public MatchPositionServiceImpl(
            MatchRepository matchRepository,
            MatchRegistrationService registrationService
    ) {
        this.matchRepository = matchRepository;
        this.registrationService = registrationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatchPositionOverviewDTO getPositionOverviewForMatch(Long matchId) {
        MatchEntity match = findMatchOrThrow(matchId);

        Integer maxPlayers = match.getMaxPlayers();
        MatchMode mode = match.getMatchMode();

        if (maxPlayers == null || maxPlayers <= 0 || mode == null) {
            MatchPositionOverviewDTO dto = new MatchPositionOverviewDTO();
            dto.setMatchId(matchId);
            dto.setMatchMode(mode);
            dto.setMaxPlayers(maxPlayers);
            dto.setPositionSlots(List.of());
            return dto;
        }

        // Předpoklad: maxPlayers je celkový počet hráčů pro oba týmy dohromady.
        // Kapacita pozic se počítá pro jeden tým.
        int slotsPerTeam = maxPlayers / 2;

        Map<PlayerPosition, Integer> perTeamCapacity =
                MatchModeLayoutUtil.buildPositionCapacityForMode(mode, slotsPerTeam);

        List<MatchRegistrationDTO> registrations =
                registrationService.getRegistrationsForMatch(matchId);

        Map<PlayerPosition, Long> occupiedDark =
                computeOccupancyByPosition(registrations, Team.DARK);

        Map<PlayerPosition, Long> occupiedLight =
                computeOccupancyByPosition(registrations, Team.LIGHT);

        List<MatchPositionSlotDTO> slots = perTeamCapacity.entrySet().stream()
                .map(entry -> {
                    PlayerPosition position = entry.getKey();
                    int capacity = entry.getValue();

                    int darkCount = occupiedDark.getOrDefault(position, 0L).intValue();
                    int lightCount = occupiedLight.getOrDefault(position, 0L).intValue();

                    MatchPositionSlotDTO slotDTO = new MatchPositionSlotDTO();
                    slotDTO.setPosition(position);
                    slotDTO.setCapacityPerTeam(capacity);
                    slotDTO.setOccupiedDark(darkCount);
                    slotDTO.setOccupiedLight(lightCount);
                    slotDTO.setFreeDark(Math.max(0, capacity - darkCount));
                    slotDTO.setFreeLight(Math.max(0, capacity - lightCount));

                    return slotDTO;
                })
                .toList();

        MatchPositionOverviewDTO result = new MatchPositionOverviewDTO();
        result.setMatchId(match.getId());
        result.setMatchMode(mode);
        result.setMaxPlayers(maxPlayers);
        result.setPositionSlots(slots);

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatchTeamPositionOverviewDTO getPositionOverviewForMatchAndTeam(Long matchId, Team team) {
        MatchEntity match = findMatchOrThrow(matchId);

        Integer maxPlayers = match.getMaxPlayers();
        MatchMode mode = match.getMatchMode();

        if (maxPlayers == null || maxPlayers <= 0 || mode == null || team == null) {
            MatchTeamPositionOverviewDTO dto = new MatchTeamPositionOverviewDTO();
            dto.setMatchId(matchId);
            dto.setMatchMode(mode);
            dto.setMaxPlayers(maxPlayers);
            dto.setTeam(team);
            dto.setPositionSlots(List.of());
            return dto;
        }

        int slotsPerTeam = maxPlayers / 2;

        Map<PlayerPosition, Integer> perTeamCapacity =
                MatchModeLayoutUtil.buildPositionCapacityForMode(mode, slotsPerTeam);

        List<MatchRegistrationDTO> registrations =
                registrationService.getRegistrationsForMatch(matchId);

        Map<PlayerPosition, Long> occupiedForTeam =
                computeOccupancyByPosition(registrations, team);

        List<MatchTeamPositionSlotDTO> slots = perTeamCapacity.entrySet().stream()
                .map(entry -> {
                    PlayerPosition position = entry.getKey();
                    int capacity = entry.getValue();

                    int occupied = occupiedForTeam.getOrDefault(position, 0L).intValue();
                    int free = Math.max(0, capacity - occupied);

                    MatchTeamPositionSlotDTO slotDTO = new MatchTeamPositionSlotDTO();
                    slotDTO.setPosition(position);
                    slotDTO.setCapacity(capacity);
                    slotDTO.setOccupied(occupied);
                    slotDTO.setFree(free);

                    return slotDTO;
                })
                .toList();

        MatchTeamPositionOverviewDTO result = new MatchTeamPositionOverviewDTO();
        result.setMatchId(match.getId());
        result.setMatchMode(mode);
        result.setMaxPlayers(maxPlayers);
        result.setTeam(team);
        result.setPositionSlots(slots);

        return result;
    }

    /**
     * Počítá obsazenost pozic pro daný tým na základě registrací.
     *
     * Do obsazenosti se započítávají pouze registrace ve stavu REGISTERED.
     * Registrace bez pozice nebo s pozicí ANY se ignorují, protože nejsou
     * přiřazeny na konkrétní slot na ledě.
     *
     * @param registrations Registrace hráčů k zápasu.
     * @param team          Tým, pro který se obsazenost počítá.
     * @return Mapa pozice na počet obsazených míst v daném týmu.
     */
    private Map<PlayerPosition, Long> computeOccupancyByPosition(
            List<MatchRegistrationDTO> registrations,
            Team team
    ) {
        if (registrations == null || registrations.isEmpty()) {
            return Map.of();
        }

        return registrations.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .filter(r -> r.getTeam() == team)
                .map(MatchRegistrationDTO::getPositionInMatch)
                .filter(Objects::nonNull)
                .filter(pos -> pos != PlayerPosition.ANY)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));
    }

    /**
     * Načítá zápas podle identifikátoru nebo vyhazuje výjimku.
     *
     * @param matchId Identifikátor zápasu.
     * @return Načtená entita zápasu.
     */
    private MatchEntity findMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }
}