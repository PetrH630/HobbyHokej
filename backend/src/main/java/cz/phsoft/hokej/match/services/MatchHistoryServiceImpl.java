package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.repositories.MatchHistoryRepository;
import cz.phsoft.hokej.match.dto.MatchHistoryDTO;
import cz.phsoft.hokej.match.mappers.MatchHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementace servisní vrstvy pro práci s historií zápasů.
 *
 * Zajišťuje převod entit na DTO a volání repozitáře.
 * Nemění ani nevytváří historické záznamy – o to se starají
 * databázové triggery.
 */
@Service
public class MatchHistoryServiceImpl implements MatchHistoryService {

    private final MatchHistoryRepository repository;
    private final MatchHistoryMapper mapper;

    public MatchHistoryServiceImpl(
            MatchHistoryRepository repository,
            MatchHistoryMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<MatchHistoryDTO> getHistoryForMatch(Long matchId) {
        return mapper.toDTOList(
                repository.findByMatchIdOrderByChangedAtDesc(matchId)
        );
    }
}
