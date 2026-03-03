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

/**
 * Implementace čtecí služby pro registrace hráčů na zápasy.
 *
 * Odpovědnosti:
 * - načítání registrací z repository vrstvy,
 * - filtrování registrací podle aktuálně vybrané nebo aktivní sezóny,
 * - poskytování přehledových dat ve formě DTO pro controller vrstvu,
 * - identifikace hráčů, kteří na konkrétní zápas nereagovali.
 *
 * Implementace používá MatchRegistrationRepository, MatchRepository a PlayerRepository
 * pro přístup k datům a mapovací služby MatchRegistrationMapper a PlayerMapper
 * pro převod entit do přenosových objektů.
 *
 * Pro omezení na relevantní zápasy a registrace se používá SeasonService
 * a CurrentSeasonService.
 */
@Service
public class MatchRegistrationQueryServiceImpl implements MatchRegistrationQueryService {
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
     * Zápas se nejprve načte z repository. Pokud zápas nepatří do aktuálně
     * vybrané sezóny, vrací se prázdný seznam. Pokud do aktuální sezóny patří,
     * registrace se načtou z MatchRegistrationRepository a převedou do DTO pomocí
     * MatchRegistrationMapper.
     *
     * @param matchId identifikátor zápasu, pro který se registrace načítají
     * @return seznam registrací převedených do DTO pro daný zápas v rámci aktuální sezóny
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
     * Registrace se načítají hromadně podle daných identifikátorů a následně se filtrují
     * pomocí metody isRegistrationInCurrentSeason tak, aby zůstaly pouze registrace
     * patřící do aktuální sezóny. Výsledná data se převádějí do DTO.
     *
     * @param matchIds seznam identifikátorů zápasů
     * @return seznam registrací převedených do DTO pro zadané zápasy v rámci aktuální sezóny
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
     * Všechny registrace se načtou z MatchRegistrationRepository a poté se
     * pomocí metody isRegistrationInCurrentSeason odfiltrují tak, aby zůstaly
     * pouze registrace patřící k zápasům v aktuální sezóně. Následně se provedou
     * mapování do přenosových objektů.
     *
     * @return seznam všech registrací převedených do DTO v rámci aktuální sezóny
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
     * Registrace se načtou podle identifikátoru hráče a následně se
     * zúží na ty, které náleží k zápasům v aktuální sezóně. Výsledná
     * kolekce se převede do DTO pomocí MatchRegistrationMapper.
     *
     * @param playerId identifikátor hráče
     * @return seznam registrací hráče převedených do DTO v rámci aktuální sezóny
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
     * Zápas se nejprve ověří vůči aktuálně vybrané sezóně. Pokud
     * do aktuální sezóny nepatří, vrací se prázdný seznam. V opačném
     * případě se načte množina hráčů, kteří mají k danému zápasu
     * uloženou registraci v jakémkoliv stavu. Z PlayerRepository se
     * načtou všichni hráči a odfiltrují se ti, kteří již reagovali.
     * Zbývající hráči se mapují do DTO pomocí PlayerMapper.
     *
     * @param matchId identifikátor zápasu
     * @return seznam hráčů bez reakce převedených do DTO v rámci aktuální sezóny
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
     * Metoda se používá pro zajištění konzistentního získání entity zápasu
     * ve všech veřejných metodách této služby. Při neexistenci zápasu se
     * vyhazuje MatchNotFoundException.
     *
     * @param matchId identifikátor zápasu
     * @return načtená entita zápasu
     */
    private MatchEntity getMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    /**
     * Vyhodnocuje, zda zápas patří do aktuálně vybrané sezóny.
     *
     * Zápas se považuje za relevantní, pokud má přiřazenou sezónu a identifikátor
     * této sezóny odpovídá identifikátoru sezóny vrácené metodou getCurrentSeasonIdOrActive.
     *
     * @param match zápas, který se má vyhodnotit
     * @return true, pokud zápas patří do aktuální sezóny, jinak false
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
     * Registrace se považuje za relevantní, pokud není null a pokud
     * přiřazený zápas splňuje podmínku aktuální sezóny dle metody
     * isMatchInCurrentSeason.
     *
     * @param registration registrace, která se má vyhodnotit
     * @return true, pokud registrace patří do aktuální sezóny, jinak false
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
     * Nejprve se zjišťuje aktuálně vybraná sezóna z CurrentSeasonService.
     * Pokud není nastavena, použije se aktivní sezóna získaná ze SeasonService.
     *
     * @return identifikátor aktuální nebo aktivní sezóny
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
     * Registrace se načtou z MatchRegistrationRepository podle identifikátoru
     * zápasu a pro každou registraci se získá identifikátor hráče. Výsledkem
     * je množina jedinečných identifikátorů hráčů, kteří na zápas reagovali.
     *
     * @param matchId identifikátor zápasu
     * @return množina identifikátorů hráčů, kteří na zápas reagovali
     */
    private Set<Long> getRespondedPlayerIds(Long matchId) {
        return registrationRepository.findByMatchId(matchId).stream()
                .map(r -> r.getPlayer().getId())
                .collect(Collectors.toSet());
    }

}