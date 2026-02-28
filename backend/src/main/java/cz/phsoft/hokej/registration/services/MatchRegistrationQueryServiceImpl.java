package cz.phsoft.hokej.registration.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.registration.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.registration.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.match.repositories.MatchRepository;
import cz.phsoft.hokej.player.repositories.PlayerRepository;
import cz.phsoft.hokej.match.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.registration.mappers.MatchRegistrationMapper;
import cz.phsoft.hokej.player.mappers.PlayerMapper;
import cz.phsoft.hokej.season.services.CurrentSeasonService;
import cz.phsoft.hokej.season.services.SeasonService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchRegistrationQueryServiceImpl implements MatchRegistrationQueryService{
    private final MatchRegistrationRepository registrationRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final MatchRegistrationMapper matchRegistrationMapper;
    private final PlayerMapper playerMapper;
    private final SeasonService seasonService;
    private final CurrentSeasonService currentSeasonService;

    public MatchRegistrationQueryServiceImpl(
            MatchRegistrationRepository registrationRepository,
            MatchRepository matchRepository,
            PlayerRepository playerRepository,
            MatchRegistrationMapper matchRegistrationMapper,
            PlayerMapper playerMapper,
            SeasonService seasonService,
            CurrentSeasonService currentSeasonService
    ) {
        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.matchRegistrationMapper = matchRegistrationMapper;
        this.playerMapper = playerMapper;
        this.seasonService = seasonService;
        this.currentSeasonService = currentSeasonService;
    }

    /**
     * Vrací registrace pro daný zápas omezené na aktuálně vybranou sezónu.
     *
     * Pokud zápas nepatří do aktuálně vybrané sezóny, vrací se prázdný seznam.
     *
     * @param matchId Identifikátor zápasu, pro který se registrace načítají.
     * @return Seznam registrací převedených do DTO pro daný zápas v rámci aktuální sezóny.
     */
    @Override
    public List<MatchRegistrationDTO> getRegistrationsForMatch(Long matchId) {
        MatchEntity match = getMatchOrThrow(matchId);

        if (!isMatchInCurrentSeason(match)) {
            return List.of();
        }

        return matchRegistrationMapper.toDTOList(
                registrationRepository.findByMatchId(matchId)
        );
    }

    /**
     * Vrací registrace pro zadanou sadu zápasů omezené na aktuálně vybranou sezónu.
     *
     * Pokud je seznam identifikátorů zápasů null nebo prázdný, vrací se prázdný seznam.
     *
     * @param matchIds Seznam identifikátorů zápasů.
     * @return Seznam registrací převedených do DTO pro zadané zápasy
     * v rámci aktuální sezóny.
     */
    @Override
    public List<MatchRegistrationDTO> getRegistrationsForMatches(List<Long> matchIds) {
        if (matchIds == null || matchIds.isEmpty()) {
            return List.of();
        }

        List<MatchRegistrationEntity> regsInSeason = registrationRepository
                .findByMatchIdIn(matchIds).stream()
                .filter(this::isRegistrationInCurrentSeason)
                .toList();

        return matchRegistrationMapper.toDTOList(regsInSeason);
    }

    /**
     * Vrací všechny registrace v systému omezené na aktuálně vybranou sezónu.
     *
     * @return Seznam všech registrací převedených do DTO v rámci aktuální sezóny.
     */
    @Override
    public List<MatchRegistrationDTO> getAllRegistrations() {
        List<MatchRegistrationEntity> regsInSeason = registrationRepository
                .findAll().stream()
                .filter(this::isRegistrationInCurrentSeason)
                .toList();

        return matchRegistrationMapper.toDTOList(regsInSeason);
    }

    /**
     * Vrací registrace zadaného hráče omezené na aktuálně vybranou sezónu.
     *
     * @param playerId Identifikátor hráče.
     * @return Seznam registrací hráče převedených do DTO v rámci aktuální sezóny.
     */
    @Override
    public List<MatchRegistrationDTO> getRegistrationsForPlayer(Long playerId) {
        List<MatchRegistrationEntity> regsInSeason = registrationRepository
                .findByPlayerId(playerId).stream()
                .filter(this::isRegistrationInCurrentSeason)
                .toList();

        return matchRegistrationMapper.toDTOList(regsInSeason);
    }

    /**
     * Vrací hráče, kteří na daný zápas nijak nereagovali.
     *
     * Pokud zápas nepatří do aktuálně vybrané sezóny, vrací se prázdný seznam.
     *
     * @param matchId Identifikátor zápasu.
     * @return Seznam hráčů bez reakce převedených do DTO v rámci aktuální sezóny.
     */
    @Override
    public List<PlayerDTO> getNoResponsePlayers(Long matchId) {
        MatchEntity match = getMatchOrThrow(matchId);

        if (!isMatchInCurrentSeason(match)) {
            return List.of();
        }

        Set<Long> respondedIds = getRespondedPlayerIds(matchId);

        List<PlayerEntity> noResponsePlayers = playerRepository.findAll().stream()
                .filter(p -> !respondedIds.contains(p.getId()))
                .toList();

        return noResponsePlayers.stream()
                .map(playerMapper::toDTO)
                .toList();
    }

    /**
     * Načítá zápas podle identifikátoru nebo vyhazuje výjimku při neexistenci.
     *
     * @param matchId Identifikátor zápasu.
     * @return Načtená entita zápasu.
     */
    private MatchEntity getMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    /**
     * Vyhodnocuje, zda zápas patří do aktuálně vybrané sezóny.
     *
     * @param match Zápas, který se má vyhodnotit.
     * @return True, pokud zápas patří do aktuální sezóny, jinak false.
     */
    private boolean isMatchInCurrentSeason(MatchEntity match) {
        if (match == null || match.getSeason() == null) {
            return false;
        }
        Long seasonId = getCurrentSeasonIdOrActive();
        return seasonId.equals(match.getSeason().getId());
    }

    /**
     * Vyhodnocuje, zda registrace patří k zápasu v aktuálně vybrané sezóně.
     *
     * @param registration Registrace, která se má vyhodnotit.
     * @return True, pokud registrace patří do aktuální sezóny, jinak false.
     */
    private boolean isRegistrationInCurrentSeason(MatchRegistrationEntity registration) {
        if (registration == null) {
            return false;
        }
        return isMatchInCurrentSeason(registration.getMatch());
    }

    /**
     * Vrací identifikátor sezóny používané pro filtrování registrací.
     *
     * @return Identifikátor aktuální nebo aktivní sezóny.
     */
    private Long getCurrentSeasonIdOrActive() {
        Long id = currentSeasonService.getCurrentSeasonIdOrDefault();
        if (id != null) {
            return id;
        }
        return seasonService.getActiveSeason().getId();
    }

    /**
     * Vrací množinu identifikátorů hráčů, kteří mají k zápasu uloženou
     * registraci v jakémkoliv stavu.
     *
     * @param matchId Identifikátor zápasu.
     * @return Množina identifikátorů hráčů, kteří na zápas reagovali.
     */
    private Set<Long> getRespondedPlayerIds(Long matchId) {
        return registrationRepository.findByMatchId(matchId).stream()
                .map(r -> r.getPlayer().getId())
                .collect(Collectors.toSet());
    }

}
