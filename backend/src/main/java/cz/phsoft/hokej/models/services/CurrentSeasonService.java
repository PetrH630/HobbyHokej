package cz.phsoft.hokej.models.services;

public interface CurrentSeasonService {

    /**
     * Vrátí ID sezóny uložené v session, nebo:
     * - pokud není nic nastaveno, vezme globálně aktivní sezónu,
     *   uloží ji do session a vrátí její ID.
     */
    Long getCurrentSeasonIdOrDefault();

    /**
     * Nastaví sezónu pro aktuálního uživatele (session).
     */
    void setCurrentSeasonId(Long seasonId);

    /**
     * Volitelně – vymaže sezónu ze session (např. při logoutu).
     */
    void clearCurrentSeason();
}
