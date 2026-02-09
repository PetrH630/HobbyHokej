package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.models.dto.SeasonHistoryDTO;

import java.util.List;

public interface SeasonHistoryService {

    List<SeasonHistoryDTO> getHistoryForSeason(Long seasonId);
}
