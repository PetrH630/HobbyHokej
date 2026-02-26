package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.enums.Team;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerStatsDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service vrstva pro výpočet statistik hráče v rámci aktuální sezóny.
 *
 * Odpovědnosti:
 * - načítání hráče a odehraných zápasů aktuální sezóny,
 * - filtrování zápasů podle data vytvoření hráče a jeho aktivity v daném termínu,
 * - agregace registrací hráče do souhrnných počtů podle {@link PlayerMatchStatus},
 * - sestavení {@link PlayerStatsDTO} včetně domovského týmu, pozic a registrací podle týmů.
 *
 * Tato třída neřeší:
 * - HTTP vrstvu a mapování requestů na DTO (řeší controllery),
 * - správu sezón obecně (řeší {@link SeasonService} a {@link CurrentSeasonService}),
 * - ukládání nebo měnění registrací (řeší {@link MatchRegistrationService}).
 *
 * Spolupracuje s:
 * - {@link PlayerRepository} pro načtení hráče,
 * - {@link MatchRepository} pro načtení zápasů aktuální sezóny,
 * - {@link CurrentSeasonService} a {@link SeasonService} pro určení sezóny,
 * - {@link PlayerInactivityPeriodService} pro vyhodnocení aktivity hráče v termínu zápasu,
 * - {@link MatchRegistrationService} pro načtení registrací na zápasy.
 */
@Service
public class PlayerStatsServiceImpl implements PlayerStatsService {

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final CurrentSeasonService currentSeasonService;
    private final SeasonService seasonService;
    private final PlayerInactivityPeriodService playerInactivityPeriodService;
    private final MatchRegistrationService matchRegistrationService;

    /**
     * Vytváří službu pro výpočet statistik hráče.
     *
     * @param playerRepository Repository pro práci s hráči.
     * @param matchRepository Repository pro práci se zápasy.
     * @param currentSeasonService Service poskytující identifikátor aktuální sezóny.
     * @param seasonService Service poskytující aktivní sezónu jako fallback.
     * @param playerInactivityPeriodService Service pro vyhodnocení aktivity hráče v termínu zápasu.
     * @param matchRegistrationService Service pro načítání registrací hráčů na zápasy.
     */
    public PlayerStatsServiceImpl(PlayerRepository playerRepository,
                                  MatchRepository matchRepository,
                                  CurrentSeasonService currentSeasonService,
                                  SeasonService seasonService,
                                  PlayerInactivityPeriodService playerInactivityPeriodService,
                                  MatchRegistrationService matchRegistrationService) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.currentSeasonService = currentSeasonService;
        this.seasonService = seasonService;
        this.playerInactivityPeriodService = playerInactivityPeriodService;
        this.matchRegistrationService = matchRegistrationService;
    }

    /**
     * Vrací statistiky hráče pro odehrané zápasy aktuální sezóny.
     *
     * Postup:
     * - načte se hráč podle ID a získá se jeho časové razítko vytvoření,
     * - načtou se všechny odehrané zápasy aktuální sezóny a spočítá se jejich celkový počet,
     * - zápasy se filtrují tak, aby byly zahrnuty pouze:
     *   - zápasy po datu vytvoření hráče,
     *   - zápasy v období, kdy je hráč aktivní (delegováno do {@link PlayerInactivityPeriodService}),
     * - pokud pro hráče nejsou žádné relevantní zápasy, sestaví se DTO
     *   s počtem zápasů v sezóně, počtem zápasů dostupných pro hráče,
     *   domovským týmem, pozicemi a prázdnou statistikou,
     * - pro relevantní zápasy se načtou registrace přes {@link MatchRegistrationService},
     *   vytvoří se mapování matchId → status a matchId → tým hráče,
     * - pro každý zápas se určí status hráče, pro zápasy bez registrace
     *   se použije status {@link PlayerMatchStatus#NO_RESPONSE},
     * - agregované počty podle statusu a počty registrací podle týmů se přenesou do DTO.
     *
     * DTO vždy obsahuje:
     * - celkový počet zápasů v sezóně,
     * - počet zápasů, které byly pro hráče relevantní,
     * - domovský tým hráče,
     * - primární a sekundární pozici,
     * - počty registrací podle statusů,
     * - mapu {@code registeredByTeam} obsahující všechny hodnoty {@link Team}
     *   (i s nulovými počty), aby měl frontend stabilní strukturu.
     *
     * @param playerId Identifikátor hráče, pro kterého se statistiky počítají.
     * @return DTO obsahující souhrnné počty zápasů podle statusů a doplňkové údaje.
     * @throws PlayerNotFoundException Pokud hráč se zadaným identifikátorem neexistuje.
     */
    @Override
    public PlayerStatsDTO getPlayerStats(Long playerId) {
        PlayerEntity player = getPlayerOrThrow(playerId);
        LocalDateTime playerCreatedDate = player.getTimestamp();

        List<MatchEntity> pastMatchesInSeason = findPastMatchesForCurrentSeason();
        int allMatchesInCurrentSeason = pastMatchesInSeason.size();

        List<MatchEntity> availableMatches =
                pastMatchesInSeason.stream()
                        .filter(match -> match.getDateTime().isAfter(playerCreatedDate))
                        .filter(match -> isPlayerActiveForMatch(player, match.getDateTime()))
                        .toList();

        int allMatchesInSeasonForPlayer = availableMatches.size();

        PlayerStatsDTO statsDTO = new PlayerStatsDTO();
        statsDTO.setPlayerId(playerId);
        statsDTO.setAllMatchesInSeason(allMatchesInCurrentSeason);
        statsDTO.setAllMatchesInSeasonForPlayer(allMatchesInSeasonForPlayer);
        statsDTO.setHomeTeam(player.getTeam());
        statsDTO.setPrimaryPosition(player.getPrimaryPosition());
        statsDTO.setSecondaryPosition(player.getSecondaryPosition());

        // vždy připravíme mapu se všemi týmy (ať je výstup stabilní pro FE)
        EnumMap<Team, Integer> registeredByTeam = new EnumMap<>(Team.class);
        for (Team t : Team.values()) {
            registeredByTeam.put(t, 0);
        }

        if (availableMatches.isEmpty()) {
            statsDTO.setRegisteredByTeam(registeredByTeam);
            return statsDTO;
        }

        List<Long> matchIds = availableMatches.stream()
                .map(MatchEntity::getId)
                .toList();

        List<MatchRegistrationDTO> allRegistrations =
                matchRegistrationService.getRegistrationsForMatches(matchIds);

        // matchId -> status hráče v daném zápase
        Map<Long, PlayerMatchStatus> playerStatusByMatchId = allRegistrations.stream()
                .filter(r -> playerId.equals(r.getPlayerId()))
                .collect(Collectors.toMap(
                        MatchRegistrationDTO::getMatchId,
                        MatchRegistrationDTO::getStatus,
                        (a, b) -> a
                ));

        // matchId -> tým hráče v daném zápase (podle registrace)
        Map<Long, Team> playerTeamByMatchId = allRegistrations.stream()
                .filter(r -> playerId.equals(r.getPlayerId()))
                .collect(Collectors.toMap(
                        MatchRegistrationDTO::getMatchId,
                        MatchRegistrationDTO::getTeam,
                        (a, b) -> a
                ));

        EnumMap<PlayerMatchStatus, Integer> counts = new EnumMap<>(PlayerMatchStatus.class);

        for (MatchEntity match : availableMatches) {
            PlayerMatchStatus status = playerStatusByMatchId.getOrDefault(
                    match.getId(),
                    PlayerMatchStatus.NO_RESPONSE
            );

            counts.merge(status, 1, Integer::sum);

            // jen REGISTERED a jen podle team z registrace
            if (status == PlayerMatchStatus.REGISTERED) {
                Team team = playerTeamByMatchId.get(match.getId());
                if (team != null) {
                    registeredByTeam.merge(team, 1, Integer::sum);
                }
            }
        }

        statsDTO.setRegistered(counts.getOrDefault(PlayerMatchStatus.REGISTERED, 0));
        statsDTO.setUnregistered(counts.getOrDefault(PlayerMatchStatus.UNREGISTERED, 0));
        statsDTO.setExcused(counts.getOrDefault(PlayerMatchStatus.EXCUSED, 0));
        statsDTO.setSubstituted(counts.getOrDefault(PlayerMatchStatus.SUBSTITUTE, 0));
        statsDTO.setReserved(counts.getOrDefault(PlayerMatchStatus.RESERVED, 0));
        statsDTO.setNoResponse(counts.getOrDefault(PlayerMatchStatus.NO_RESPONSE, 0));
        statsDTO.setNoExcused(counts.getOrDefault(PlayerMatchStatus.NO_EXCUSED, 0));

        // stabilní mapa pro FE (obsahuje všechny Team.values())
        statsDTO.setRegisteredByTeam(registeredByTeam);

        return statsDTO;
    }

    /**
     * Načítá odehrané zápasy aktuální sezóny.
     *
     * Zápasy se vybírají podle identifikátoru sezóny a data zápasu menšího než aktuální čas.
     * Výsledek se řadí sestupně podle data zápasu.
     *
     * @return Seznam odehraných zápasů aktuální sezóny.
     */
    private List<MatchEntity> findPastMatchesForCurrentSeason() {
        return matchRepository.findBySeasonIdAndDateTimeBeforeOrderByDateTimeDesc(
                getCurrentSeasonIdOrActive(),
                now()
        );
    }

    /**
     * Vrací hráče podle identifikátoru nebo vyhazuje výjimku, pokud neexistuje.
     *
     * @param playerId Identifikátor hráče.
     * @return Entita hráče.
     * @throws PlayerNotFoundException Pokud hráč se zadaným identifikátorem neexistuje.
     */
    private PlayerEntity getPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    /**
     * Určuje identifikátor sezóny, pro kterou se mají počítat statistiky.
     *
     * Primárně se používá identifikátor aktuální sezóny poskytnutý službou
     * {@link CurrentSeasonService}. Pokud není dostupný, použije se
     * identifikátor aktivní sezóny z {@link SeasonService}.
     *
     * @return Identifikátor sezóny použitý pro výběr zápasů.
     */
    private Long getCurrentSeasonIdOrActive() {
        Long id = currentSeasonService.getCurrentSeasonIdOrDefault();
        if (id != null) {
            return id;
        }
        return seasonService.getActiveSeason().getId();
    }

    /**
     * Vrací aktuální čas používaný pro porovnání termínu zápasů.
     *
     * @return Aktuální čas.
     */
    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Vyhodnocuje, zda byl hráč aktivní v době konání zápasu.
     *
     * Logika je delegována do {@link PlayerInactivityPeriodService},
     * která zohledňuje období neaktivity hráče.
     *
     * @param player Hráč, pro kterého se aktivita vyhodnocuje.
     * @param dateTime Termín zápasu.
     * @return True, pokud byl hráč v daném termínu aktivní.
     */
    private boolean isPlayerActiveForMatch(PlayerEntity player, LocalDateTime dateTime) {
        return playerInactivityPeriodService.isActive(player, dateTime);
    }
}