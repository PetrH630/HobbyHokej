package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.repositories.AppUserHistoryRepository;
import cz.phsoft.hokej.models.dto.AppUserHistoryDTO;
import cz.phsoft.hokej.models.mappers.AppUserHistoryMapper;

import java.util.List;

public class AppUserHistoryServiceImpl implements AppUserHistoryService{

    private final AppUserHistoryRepository repository;
    private final AppUserHistoryMapper mapper;

    public AppUserHistoryServiceImpl(
            AppUserHistoryRepository repository,
            AppUserHistoryMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<AppUserHistoryDTO> getHistoryForUser(Long userId) {
        return mapper.toDTOList(
                repository.findByUserIdOrderByChangedAtDesc(userId)
        );
    }
}
