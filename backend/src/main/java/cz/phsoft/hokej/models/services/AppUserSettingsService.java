package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.AppUserSettingsEntity;
import cz.phsoft.hokej.models.dto.AppUserSettingsDTO;

/**
 * Service se používá pro práci s uživatelským nastavením.
 *
 * Odpovědností je získávání, vytváření a aktualizace nastavení
 * v kontextu uživatelského účtu, nikoli v kontextu hráče.
 * V této vrstvě se používá typ AppUserSettingsDTO, aby byla
 * oddělena prezentační vrstva od perzistentních entit.
 */
public interface AppUserSettingsService {

    /**
     * Vrátí nastavení pro uživatele identifikovaného e-mailem.
     * Pokud nastavení neexistuje, vytvoří se nový záznam
     * s výchozími hodnotami a uloží se k danému uživateli.
     *
     * Metoda se typicky používá v controlleru pro načtení
     * dat do formuláře na frontendu.
     *
     * @param userEmail e-mail uživatele, který slouží jako unikátní login
     * @return nastavení uživatele převedené do AppUserSettingsDTO
     */
    AppUserSettingsDTO getSettingsForUser(String userEmail);

    /**
     * Aktualizuje nastavení pro uživatele identifikovaného e-mailem.
     * Pokud uživatel nemá dosud žádné nastavení, vytvoří se nejprve
     * výchozí záznam a poté se na něj aplikují hodnoty z DTO.
     *
     * Metoda se obvykle volá z controlleru po odeslání formuláře
     * s uživatelskými preferencemi.
     *
     * @param userEmail e-mail uživatele, pro kterého se nastavení aktualizuje
     * @param dto nové hodnoty nastavení z frontendu
     * @return aktuální stav nastavení po uložení v podobě AppUserSettingsDTO
     */
    AppUserSettingsDTO updateSettingsForUser(String userEmail, AppUserSettingsDTO dto);

    /**
     * Vytvoří výchozí nastavení pro daného uživatele.
     *
     * Metoda se používá interně při prvním přístupu k nastavení,
     * kdy neexistuje žádný záznam v tabulce s nastavením.
     * Výchozí hodnoty se nastaví tak, aby byla aplikace použitelná
     * i bez ručního nastavení.
     *
     * @param user entita uživatele, ke které se nastavení naváže
     * @return nově vytvořená entita AppUserSettingsEntity s výchozími hodnotami
     */
    AppUserSettingsEntity createDefaultSettingsForUser(AppUserEntity user);

}
