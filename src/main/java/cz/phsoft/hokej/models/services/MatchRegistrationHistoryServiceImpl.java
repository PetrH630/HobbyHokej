package cz.phsoft.hokej.models.services.impl;

import cz.phsoft.hokej.data.entities.MatchRegistrationHistoryEntity;
import cz.phsoft.hokej.data.repositories.MatchRegistrationHistoryRepository;
import cz.phsoft.hokej.models.dto.MatchRegistrationHistoryDTO;
import cz.phsoft.hokej.models.services.MatchRegistrationHistoryService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchRegistrationHistoryServiceImpl implements MatchRegistrationHistoryService {

    private final MatchRegistrationHistoryRepository repository;

    public MatchRegistrationHistoryServiceImpl(MatchRegistrationHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public MatchRegistrationHistoryDTO save(MatchRegistrationHistoryDTO dto) {

        // Převod DTO → Entity manuálně
        MatchRegistrationHistoryEntity entity = new MatchRegistrationHistoryEntity();
        entity.setMatchRegistrationId(dto.getMatchRegistrationId());
        entity.setMatchId(dto.getMatchId());
        entity.setPlayerId(dto.getPlayerId());
        entity.setStatus(dto.getStatus());
        entity.setExcuseReason(dto.getExcuseReason());
        entity.setExcuseNote(dto.getExcuseNote());
        entity.setAdminNote(dto.getAdminNote());
        entity.setJerseyColor(dto.getJerseyColor());
        entity.setOriginalTimestamp(dto.getOriginalTimestamp() != null ? dto.getOriginalTimestamp() : LocalDateTime.now());
        entity.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "system");
        entity.setAction(dto.getAction() != null ? dto.getAction() : "INSERT");
        entity.setChangedAt(LocalDateTime.now());

        MatchRegistrationHistoryEntity saved = repository.save(entity);

        // Převod Entity → DTO manuálně
        MatchRegistrationHistoryDTO result = new MatchRegistrationHistoryDTO();
        result.setId(saved.getId());
        result.setMatchRegistrationId(saved.getMatchRegistrationId());
        result.setMatchId(saved.getMatchId());
        result.setPlayerId(saved.getPlayerId());
        result.setStatus(saved.getStatus());
        result.setExcuseReason(saved.getExcuseReason());
        result.setExcuseNote(saved.getExcuseNote());
        result.setAdminNote(saved.getAdminNote());
        result.setJerseyColor(saved.getJerseyColor());
        result.setOriginalTimestamp(saved.getOriginalTimestamp());
        result.setCreatedBy(saved.getCreatedBy());
        result.setAction(saved.getAction());
        result.setChangedAt(saved.getChangedAt());

        return result;
    }

    @Override
    public List<MatchRegistrationHistoryDTO> getHistoryForRegistration(Long registrationId) {
        return repository.findByMatchRegistrationIdOrderByChangedAtDesc(registrationId)
                .stream()
                .map(e -> {
                    MatchRegistrationHistoryDTO dto = new MatchRegistrationHistoryDTO();
                    dto.setId(e.getId());
                    dto.setMatchRegistrationId(e.getMatchRegistrationId());
                    dto.setMatchId(e.getMatchId());
                    dto.setPlayerId(e.getPlayerId());
                    dto.setStatus(e.getStatus());
                    dto.setExcuseReason(e.getExcuseReason());
                    dto.setExcuseNote(e.getExcuseNote());
                    dto.setAdminNote(e.getAdminNote());
                    dto.setJerseyColor(e.getJerseyColor());
                    dto.setOriginalTimestamp(e.getOriginalTimestamp());
                    dto.setCreatedBy(e.getCreatedBy());
                    dto.setAction(e.getAction());
                    dto.setChangedAt(e.getChangedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
