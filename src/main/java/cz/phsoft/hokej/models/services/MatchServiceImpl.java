package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.mappers.MatchMapper;
import cz.phsoft.hokej.models.services.MatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final MatchMapper matchMapper;

    public MatchServiceImpl(MatchRepository matchRepository,
                            PlayerRepository playerRepository,
                            MatchMapper matchMapper) {
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.matchMapper = matchMapper;
    }

    @Override
    @Transactional
    public MatchDTO prihlasitHrace(Long matchId, Long playerId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Zápas nenalezen"));
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Hráč nenalezen"));

        match.getPlayers().add(player);
        player.getMatches().add(match);

        return matchMapper.toDTO(match);
    }

    @Override
    @Transactional
    public MatchDTO odhlasitHrace(Long matchId, Long playerId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Zápas nenalezen"));
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Hráč nenalezen"));

        match.getPlayers().remove(player);
        player.getMatches().remove(match);

        return matchMapper.toDTO(match);
    }
}

