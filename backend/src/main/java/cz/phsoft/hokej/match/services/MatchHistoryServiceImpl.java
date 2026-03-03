package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.repositories.MatchHistoryRepository;
import cz.phsoft.hokej.match.dto.MatchHistoryDTO;
import cz.phsoft.hokej.match.mappers.MatchHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementace servisní vrstvy pro práci s historií zápasů.
 *
 * Třída zajišťuje:
 * - načtení historických entit z repozitáře,
 * - převod entit na DTO pomocí mapperu.
 *
 * Nevytváří ani neupravuje historické záznamy.
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

    /**
     * {@inheritDoc}
     *
     * Deleguje načtení dat do repozitáře
     * a převod entit na DTO do mapperu.
     */
    @Override
    public List<MatchHistoryDTO> getHistoryForMatch(Long matchId) {
        return mapper.toDTOList(
                repository.findByMatchIdOrderByChangedAtDesc(matchId)
        );
    }
}