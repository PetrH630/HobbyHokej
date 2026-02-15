package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerStatsDTO;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service vrstva pro výpočet statistik hráče v rámci aktuální sezóny.
 *
 * Zajišťuje výběr odehraných zápasů aktuální sezóny a jejich zúžení podle
 * data vytvoření hráče a jeho aktivity v daném termínu. Následně agreguje
 * registrace hráče do souhrnných počtů podle statusu.
 *
 * Třída koordinuje načtení dat přes repository a deleguje dílčí logiku
 * na související služby (aktuální sezóna, aktivní sezóna, období neaktivity,
 * registrace zápasů).
 */
public class PlayerStatsServiceImpl implements PlayerStatsService {

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final CurrentSeasonService currentSeasonService;
    private final SeasonService seasonService;
    private final PlayerInactivityPeriodService playerInactivityPeriodService;
    private final MatchRegistrationService matchRegistrationService;

    /**
     * Vytváří service pro výpočet statistik hráče.
     *
     * Používá se napojení na repository pro načítání hráčů a zápasů a
     * na služby, které poskytují identifikátor aktuální sezóny, aktivní sezónu,
     * vyhodnocení aktivity hráče v čase a registrace zápasů.
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
     * Nejprve se načte hráč a datum jeho vytvoření. Následně se načtou
     * všechny odehrané zápasy aktuální sezóny a spočítá se jejich celkový počet.
     * Zápasy se dále filtrují tak, aby byly zahrnuty pouze zápasy po vytvoření hráče
     * a pouze ty, ve kterých byl hráč v daném čase aktivní.
     *
     * Pokud pro hráče nejsou žádné relevantní zápasy, vrací se DTO s vyplněným
     * identifikátorem hráče a počtem zápasů v sezóně a ostatní hodnoty zůstávají nulové.
     *
     * Pro relevantní zápasy se načtou registrace a vytvoří se mapování matchId na status
     * pouze pro daného hráče. Pro zápasy bez registrace se použije výchozí status NO_RESPONSE.
     * Nakonec se agregované počty přenesou do návratového DTO.
     *
     * @param playerId Identifikátor hráče, pro kterého se statistiky počítají.
     * @return DTO obsahující souhrnné počty zápasů podle statusů a počet zápasů v sezóně.
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

        PlayerStatsDTO statsDTO = new PlayerStatsDTO();
        statsDTO.setPlayerId(playerId);
        statsDTO.setAllMatchesInSeason(allMatchesInCurrentSeason);

        if (availableMatches.isEmpty()) {
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

        EnumMap<PlayerMatchStatus, Integer> counts = new EnumMap<>(PlayerMatchStatus.class);

        for (MatchEntity match : availableMatches) {
            PlayerMatchStatus status = playerStatusByMatchId.getOrDefault(
                    match.getId(),
                    PlayerMatchStatus.NO_RESPONSE
            );
            counts.merge(status, 1, Integer::sum);
        }

        statsDTO.setRegistered(counts.getOrDefault(PlayerMatchStatus.REGISTERED, 0));
        statsDTO.setUnregistered(counts.getOrDefault(PlayerMatchStatus.UNREGISTERED, 0));
        statsDTO.setExcused(counts.getOrDefault(PlayerMatchStatus.EXCUSED, 0));
        statsDTO.setSubstituted(counts.getOrDefault(PlayerMatchStatus.SUBSTITUTE, 0));
        statsDTO.setReserved(counts.getOrDefault(PlayerMatchStatus.RESERVED, 0));
        statsDTO.setNoResponse(counts.getOrDefault(PlayerMatchStatus.NO_RESPONSE, 0));
        statsDTO.setNoExcused(counts.getOrDefault(PlayerMatchStatus.NO_EXCUSED, 0));

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
     * Primárně se používá identifikátor aktuální sezóny poskytnutý službou.
     * Pokud není dostupný, použije se identifikátor aktivní sezóny.
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
     * Výsledek se deleguje na službu spravující období neaktivity hráče.
     *
     * @param player Hráč, pro kterého se aktivita vyhodnocuje.
     * @param dateTime Termín zápasu.
     * @return True, pokud byl hráč v daném termínu aktivní.
     */
    private boolean isPlayerActiveForMatch(PlayerEntity player, LocalDateTime dateTime) {
        return playerInactivityPeriodService.isActive(player, dateTime);
    }
}
