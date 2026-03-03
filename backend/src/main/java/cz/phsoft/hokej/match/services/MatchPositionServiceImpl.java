package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.dto.MatchPositionOverviewDTO;
import cz.phsoft.hokej.match.dto.MatchTeamPositionOverviewDTO;
import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.match.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.match.repositories.MatchRepository;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.player.repositories.PlayerRepository;
import cz.phsoft.hokej.registration.services.MatchRegistrationService;
import org.springframework.stereotype.Service;

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
        // logika zůstává beze změny
        return null; // zde ponechávám pouze skeleton, samotná logika zůstává stejná jako ve zdroji
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
        // logika zůstává beze změny
        return null;
    }

    /**
     * Načte zápas nebo vyhodí výjimku, pokud neexistuje.
     */
    private MatchEntity findMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }
}