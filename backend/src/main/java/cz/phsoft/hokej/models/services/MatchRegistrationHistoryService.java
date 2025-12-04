package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.models.dto.MatchRegistrationHistoryDTO;

import java.util.List;

public interface MatchRegistrationHistoryService {
    MatchRegistrationHistoryDTO save(MatchRegistrationHistoryDTO dto);

    List<MatchRegistrationHistoryDTO> getHistoryForRegistration(Long registrationId);

    void delete(Long id);

}
