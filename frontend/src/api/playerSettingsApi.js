// src/api/playerSettingsApi.js
import api from "./axios";

/**
 * Načte nastavení aktuálního hráče (/api/me/settings).
 */
export const getCurrentPlayerSettings = async () => {
    const res = await api.get("/me/settings", {
        withCredentials: true,
    });
    return res.data; // PlayerSettingsDTO
};

/**
 * Uloží nastavení aktuálního hráče (/api/me/settings).
 *
 * @param {Object} settings PlayerSettingsDTO
 */
export const updateCurrentPlayerSettings = async (settings) => {
    const res = await api.patch("/me/settings", settings, {
        withCredentials: true,
    });
    return res.data; // PlayerSettingsDTO
};

/**
 * (Volitelné) Načtení nastavení libovolného hráče podle ID.
 */
export const getPlayerSettings = async (playerId) => {
    const res = await api.get(`/players/${playerId}/settings`, {
        withCredentials: true,
    });
    return res.data; // PlayerSettingsDTO
};

/**
 * (Volitelné) Aktualizace nastavení libovolného hráče podle ID.
 */
export const updatePlayerSettings = async (playerId, settings) => {
    const res = await api.patch(`/players/${playerId}/settings`, settings, {
        withCredentials: true,
    });
    return res.data; // PlayerSettingsDTO
};
