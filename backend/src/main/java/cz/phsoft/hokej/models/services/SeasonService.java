package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.SeasonEntity;
import cz.phsoft.hokej.models.dto.SeasonDTO;

import java.util.List;

public interface SeasonService {

    SeasonDTO createSeason(SeasonDTO season);
    SeasonDTO updateSeason(Long id, SeasonDTO season);
    SeasonEntity getActiveSeason();
    List<SeasonDTO> getAllSeasons();
    void setActiveSeason(Long seasonId);

}
