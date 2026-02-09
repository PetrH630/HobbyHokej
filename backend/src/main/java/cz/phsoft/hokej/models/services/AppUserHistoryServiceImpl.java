package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.repositories.AppUserHistoryRepository;
import cz.phsoft.hokej.models.dto.AppUserHistoryDTO;
import cz.phsoft.hokej.models.mappers.AppUserHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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

    public List<AppUserHistoryDTO> getHistoryForUser(String email) {
        return mapper.toDTOList(
                repository.findByEmailOrderByChangedAtDesc(email)
        );
    }

    public List<AppUserHistoryDTO> getHistoryForUser(Long id) {
        return mapper.toDTOList(
                repository.findByUserIdOrderByChangedAtDesc(id)
        );
    }



}
