package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.dto.*;
import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.match.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.match.repositories.MatchRepository;
import cz.phsoft.hokej.match.util.MatchModeLayoutUtil;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.player.repositories.PlayerRepository;
import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.registration.services.MatchRegistrationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementace servisní vrstvy pro přehled obsazenosti pozic v zápase.
 *
 * Třída kombinuje:
 * - konfiguraci zápasu (MatchMode, maxPlayers),
 * - aktuální registrace hráčů,
 * - informace o hráčích z PlayerRepository.
 *
 * Výsledkem je agregovaný pohled na kapacitu a obsazenost pozic
 * pro oba týmy nebo pro konkrétní tým.
 */
@Service
public class MatchPositionServiceImpl implements MatchPositionService {

    private final MatchRepository matchRepository;
    private final MatchRegistrationService registrationService;
    private final PlayerRepository playerRepository;

    public MatchPositionServiceImpl(
            MatchRepository matchRepository,
            MatchRegistrationService registrationService,
            PlayerRepository playerRepository
    ) {
        this.matchRepository = matchRepository;
        this.registrationService = registrationService;
        this.playerRepository = playerRepository;
    }

    /**
     * Vrací přehled obsazenosti pozic pro oba týmy.
     *
     * Metoda:
     * - načte zápas,
     * - vypočítá kapacitu pozic podle MatchMode,
     * - spočítá obsazenost pozic pro DARK a LIGHT,
     * - sestaví výsledné DTO.
     *
     * @param matchId identifikátor zápasu
     * @return přehled obsazenosti pozic
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
     * Vrací přehled obsazenosti pozic pro konkrétní tým.
     *
     * Metoda:
     * - načte zápas,
     * - vypočítá kapacitu pozic pro jeden tým,
     * - spočítá obsazenost REGISTERED hráčů,
     * - doplní seznam hráčů REGISTERED a RESERVED,
     * - vrátí přehledové DTO.
     *
     * @param matchId identifikátor zápasu
     * @param team tým DARK nebo LIGHT
     * @return přehled obsazenosti pozic pro daný tým
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

        // hráči na ledě
        Map<PlayerPosition, List<MatchTeamPositionPlayerDTO>> registeredByPosition =
                buildPlayersByPositionForTeamAndStatus(registrations, team, PlayerMatchStatus.REGISTERED);

        // náhradníci
        Map<PlayerPosition, List<MatchTeamPositionPlayerDTO>> reservedByPosition =
                buildPlayersByPositionForTeamAndStatus(registrations, team, PlayerMatchStatus.RESERVED);

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
                    slotDTO.setRegisteredPlayers(
                            registeredByPosition.getOrDefault(position, List.of())
                    );
                    slotDTO.setReservedPlayers(
                            reservedByPosition.getOrDefault(position, List.of())
                    );

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

    /**
     * Sestavuje mapu hráčů podle pozic pro daný tým.
     *
     * Zahrnují se pouze registrace ve stavu REGISTERED, s konkrétní pozicí
     * (pozice ANY se ignoruje).
     *
     * Jméno hráče se dotahuje z PlayerEntity podle playerId z MatchRegistrationDTO.
     *
     * @param registrations Registrace hráčů k zápasu.
     * @param team          Tým, pro který se přehled tvoří.
     * @return Mapa pozice na seznam hráčů přiřazených na dané pozici.
     */
    private Map<PlayerPosition, List<MatchTeamPositionPlayerDTO>> buildPlayersByPositionForTeamAndStatus(
            List<MatchRegistrationDTO> registrations,
            Team team,
            PlayerMatchStatus status
    ) {
        if (registrations == null || registrations.isEmpty()) {
            return Map.of();
        }

        List<Long> playerIds = registrations.stream()
                .filter(r -> r.getStatus() == status)
                .filter(r -> r.getTeam() == team)
                .filter(r -> r.getPositionInMatch() != null)
                .filter(r -> r.getPositionInMatch() != PlayerPosition.ANY)
                .map(MatchRegistrationDTO::getPlayerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (playerIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, PlayerEntity> playersById = playerRepository.findAllById(playerIds).stream()
                .collect(Collectors.toMap(PlayerEntity::getId, Function.identity()));

        return registrations.stream()
                .filter(r -> r.getStatus() == status)
                .filter(r -> r.getTeam() == team)
                .filter(r -> r.getPositionInMatch() != null)
                .filter(r -> r.getPositionInMatch() != PlayerPosition.ANY)
                .collect(Collectors.groupingBy(
                        MatchRegistrationDTO::getPositionInMatch,
                        Collectors.mapping(r -> {
                            MatchTeamPositionPlayerDTO dto = new MatchTeamPositionPlayerDTO();
                            Long playerId = r.getPlayerId();
                            dto.setPlayerId(playerId);

                            PlayerEntity player = playersById.get(playerId);
                            if (player != null) {
                                // Uprav dle své entity (fullName / firstName / lastName / nickname)
                                String fullName = player.getFullName();
                                dto.setPlayerName(fullName != null ? fullName.trim() : null);
                            } else {
                                dto.setPlayerName(null);
                            }

                            return dto;
                        }, Collectors.toList())
                ));
    }
}