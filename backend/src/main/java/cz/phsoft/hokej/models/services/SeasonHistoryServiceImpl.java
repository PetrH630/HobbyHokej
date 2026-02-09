package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.repositories.SeasonHistoryRepository;
import cz.phsoft.hokej.models.dto.SeasonHistoryDTO;
import cz.phsoft.hokej.models.mappers.SeasonHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeasonHistoryServiceImpl implements SeasonHistoryService {

    private final SeasonHistoryRepository repository;
    private final SeasonHistoryMapper mapper;

    public SeasonHistoryServiceImpl(
            SeasonHistoryRepository repository,
            SeasonHistoryMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<SeasonHistoryDTO> getHistoryForSeason(Long seasonId) {
        return mapper.toDTOList(
                repository.findBySeasonIdOrderByChangedAtDesc(seasonId)
        );
    }
}
