package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.models.dto.AppUserHistoryDTO;

import java.util.List;

public interface AppUserHistoryService {
    public List<AppUserHistoryDTO> getHistoryForUser(Long userId);

}
