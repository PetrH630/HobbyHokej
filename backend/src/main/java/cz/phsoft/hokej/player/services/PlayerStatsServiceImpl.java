package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.season.services.CurrentSeasonService;
import cz.phsoft.hokej.registration.services.MatchRegistrationService;
import cz.phsoft.hokej.season.services.SeasonService;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.match.repositories.MatchRepository;
import cz.phsoft.hokej.player.repositories.PlayerRepository;
import cz.phsoft.hokej.player.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.player.dto.PlayerStatsDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service vrstva pro výpočet statistik hráče v rámci aktuální sezóny.
 *
 * Odpovědnost:
 * - načtení hráče a odehraných zápasů aktuální sezóny,
 * - filtrování zápasů podle data vytvoření hráče a jeho aktivity v daném termínu,
 * - agregace registrací hráče do souhrnných počtů podle PlayerMatchStatus,
 * - sestavení PlayerStatsDTO včetně domovského týmu, pozic a registrací podle týmů.
 *
 * Třída neřeší:
 * - HTTP vrstvu a mapování requestů na DTO,
 * - obecnou správu sezón,
 * - ukládání nebo změny registrací.
 *
 * Spolupracuje s repository a dalšími service vrstvami
 * pro získání potřebných dat a vyhodnocení doménových pravidel.
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
     * Závislosti jsou injektovány konstruktorově a používají se
     * pro načtení hráče, zápasů aktuální sezóny a registrací.
     *
     * @param playerRepository repository pro práci s hráči
     * @param matchRepository repository pro práci se zápasy
     * @param currentSeasonService služba poskytující identifikátor aktuální sezóny
     * @param seasonService služba poskytující aktivní sezónu jako fallback
     * @param playerInactivityPeriodService služba pro vyhodnocení aktivity hráče
     * @param matchRegistrationService služba pro načítání registrací hráčů na zápasy
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
     * Vrátí statistiky hráče pro odehrané zápasy aktuální sezóny.
     *
     * Postup výpočtu:
     * - načte se hráč podle identifikátoru,
     * - načtou se všechny odehrané zápasy aktuální sezóny,
     * - zápasy se filtrují podle data vytvoření hráče a jeho aktivity,
     * - načtou se registrace hráče na relevantní zápasy,
     * - agregují se počty podle statusů a týmů,
     * - sestaví se DTO s kompletními statistickými údaji.
     *
     * DTO vždy obsahuje:
     * - celkový počet zápasů v sezóně,
     * - počet zápasů relevantních pro hráče,
     * - domovský tým a pozice hráče,
     * - počty registrací podle statusů,
     * - mapu registeredByTeam obsahující všechny hodnoty Team.
     *
     * @param playerId identifikátor hráče, pro kterého se statistiky počítají
     * @return DTO obsahující souhrnné statistiky hráče
     * @throws PlayerNotFoundException pokud hráč se zadaným identifikátorem neexistuje
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

        Map<Long, PlayerMatchStatus> playerStatusByMatchId = allRegistrations.stream()
                .filter(r -> playerId.equals(r.getPlayerId()))
                .collect(Collectors.toMap(
                        MatchRegistrationDTO::getMatchId,
                        MatchRegistrationDTO::getStatus,
                        (a, b) -> a
                ));

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
        statsDTO.setRegisteredByTeam(registeredByTeam);

        return statsDTO;
    }

    /**
     * Načte odehrané zápasy aktuální sezóny.
     *
     * Zápasy jsou filtrovány podle identifikátoru sezóny
     * a podle data menšího než aktuální čas.
     *
     * @return seznam odehraných zápasů aktuální sezóny
     */
    private List<MatchEntity> findPastMatchesForCurrentSeason() {
        return matchRepository.findBySeasonIdAndDateTimeBeforeOrderByDateTimeDesc(
                getCurrentSeasonIdOrActive(),
                now()
        );
    }

    /**
     * Najde hráče podle identifikátoru nebo vyhodí výjimku.
     *
     * @param playerId identifikátor hráče
     * @return entita hráče
     * @throws PlayerNotFoundException pokud hráč neexistuje
     */
    private PlayerEntity getPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    /**
     * Určí identifikátor sezóny, pro kterou se mají počítat statistiky.
     *
     * Nejprve se použije identifikátor aktuální sezóny.
     * Pokud není dostupný, použije se identifikátor aktivní sezóny.
     *
     * @return identifikátor sezóny použitý pro výběr zápasů
     */
    private Long getCurrentSeasonIdOrActive() {
        Long id = currentSeasonService.getCurrentSeasonIdOrDefault();
        if (id != null) {
            return id;
        }
        return seasonService.getActiveSeason().getId();
    }

    /**
     * Vrátí aktuální čas používaný při filtrování zápasů.
     *
     * @return aktuální čas
     */
    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Vyhodnotí, zda byl hráč aktivní v době konání zápasu.
     *
     * Logika je delegována do PlayerInactivityPeriodService.
     *
     * @param player hráč, pro kterého se aktivita vyhodnocuje
     * @param dateTime termín zápasu
     * @return true, pokud byl hráč v daném termínu aktivní
     */
    private boolean isPlayerActiveForMatch(PlayerEntity player, LocalDateTime dateTime) {
        return playerInactivityPeriodService.isActive(player, dateTime);
    }
}