// src/api/userSettingsApi.js
import api from "./axios";

/**
 * Načte nastavení aktuálně přihlášeného uživatele (/api/user/settings).
 */
export const getUserSettings = async () => {
    const res = await api.get("/user/settings", {
        withCredentials: true,
    });
    return res.data; // AppUserSettingsDTO
};

/**
 * Aktualizuje nastavení aktuálně přihlášeného uživatele (/api/user/settings).
 *
 * @param {Object} settings AppUserSettingsDTO
 */
export const updateUserSettings = async (settings) => {
    const res = await api.patch("/user/settings", settings, {
        withCredentials: true,
    });
    return res.data; // AppUserSettingsDTO
};
 