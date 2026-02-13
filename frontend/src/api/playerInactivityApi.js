// src/api/playerInactivityApi.js
import api from "./axios";

/**
 * ADMIN: vrátí všechny záznamy neaktivity všech hráčů.
 * GET /api/inactivity/admin/all
 */
export const getAllPlayerInactivityPeriodsAdmin = async () => {
    const res = await api.get("/inactivity/admin/all", {
        withCredentials: true,
    });
    return res.data; // pole PlayerInactivityPeriodDTO
};

/**
 * ADMIN/MANAGER: všechna období neaktivity daného hráče
 * GET /api/inactivity/admin/player/{playerId}
 */
export const getInactivityByPlayerAdmin = async (playerId) => {
    const res = await api.get(`/inactivity/admin/player/${playerId}`);
    return res.data; // List<PlayerInactivityPeriodDTO>
};

/**
 * ADMIN/MANAGER: vytvoření období neaktivity
 * POST /api/inactivity/admin
 */
export const createInactivityAdmin = async (payload) => {
    const res = await api.post("/inactivity/admin", payload);
    return res.data; // PlayerInactivityPeriodDTO
};

/**
 * ADMIN/MANAGER: update období neaktivity
 * PUT /api/inactivity/admin/{id}
 */
export const updateInactivityAdmin = async (id, payload) => {
    const res = await api.put(`/inactivity/admin/${id}`, payload);
    return res.data;
};

/**
 * ADMIN: smazání období neaktivity
 * DELETE /api/inactivity/admin/{id}
 */
export const deleteInactivityAdmin = async (id) => {
    await api.delete(`/inactivity/admin/${id}`);
};

/**
 * PLAYER: všechna období neaktivity přihlášeného hráče
 * GET /api/inactivity/admin/me/all
 */
export const getMyInactivity = async () => {
    const res = await api.get(`/inactivity/admin/me/all`);
    return res.data; // List<PlayerInactivityPeriodDTO>
};
