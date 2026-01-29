package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.AppUserSettingsEntity;
import cz.phsoft.hokej.models.dto.AppUserSettingsDTO;

/**
 * Service pro práci s nastavením uživatele (AppUserSettingsEntity).
 *
 * Pracuje v kontextu účtu (user), nikoliv currentPlayer.
 */
public interface AppUserSettingsService {

    /**
     * Vrátí nastavení pro uživatele identifikovaného emailem.
     * Pokud nastavení ještě neexistuje, vytvoří se s default hodnotami.
     *
     * @param userEmail email uživatele (unikátní login)
     * @return nastavení uživatele ve formě DTO
     */
    AppUserSettingsDTO getSettingsForUser(String userEmail);

    /**
     * Aktualizuje nastavení pro uživatele identifikovaného emailem.
     *
     * @param userEmail email uživatele
     * @param dto       nové nastavení
     * @return aktualizované nastavení
     */
    AppUserSettingsDTO updateSettingsForUser(String userEmail, AppUserSettingsDTO dto);

    AppUserSettingsEntity createDefaultSettingsForUser(AppUserEntity user);

}
