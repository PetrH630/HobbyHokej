// src/api/seasonApi.js

import api from "./axios";

/* =========================================================
   USER – SEZÓNY PŘIHLÁŠENÉHO UŽIVATELE (/me)
   ========================================================= */

/**
 * Vrací seznam všech sezón pro přihlášeného uživatele.
 * GET /api/seasons/me
 */
export const fetchSeasonsForUser = async () => {
    const res = await api.get("/seasons/me");
    return res.data; // List<SeasonDTO>
};

/**
 * Vrací aktuálně vybranou sezónu pro uživatele.
 * GET /api/seasons/me/current
 */
export const fetchCurrentSeasonForUser = async () => {
    const res = await api.get("/seasons/me/current");
    return res.data; // SeasonDTO nebo null
};

/**
 * Nastaví aktuální sezónu pro uživatele.
 * POST /api/seasons/me/current/{seasonId}
 */
export const setCurrentSeasonForUser = async (seasonId) => {
    const res = await api.post(`/seasons/me/current/${seasonId}`);
    return res.data;
};

/* =========================================================
   ADMIN / MANAGER – GLOBÁLNÍ SPRÁVA SEZÓN
   ========================================================= */

/**
 * Načte všechny sezóny v systému.
 * GET /api/seasons
 * Role: ADMIN, MANAGER
 */
export const getAllSeasonsAdmin = async () => {
    const res = await api.get("/seasons");
    return res.data; // List<SeasonDTO>
};

/**
 * Detail sezóny podle ID.
 * GET /api/seasons/{id}
 * Role: ADMIN, MANAGER
 */
export const getSeasonByIdAdmin = async (id) => {
    const res = await api.get(`/seasons/${id}`);
    return res.data; // SeasonDTO
};

/**
 * Vytvoří novou sezónu.
 * POST /api/seasons
 * Role: ADMIN
 */
export const createSeasonAdmin = async (data) => {
    const res = await api.post("/seasons", data);
    return res.data; // SeasonDTO
};

/**
 * Aktualizuje existující sezónu.
 * PUT /api/seasons/{id}
 * Role: ADMIN
 */
export const updateSeasonAdmin = async (id, data) => {
    const res = await api.put(`/seasons/${id}`, data);
    return res.data; // SeasonDTO
};

// PATCH /api/seasons/{id}/activate
export const setActiveSeasonAdmin = async (seasonId) => {
    const res = await api.put(`/seasons/${seasonId}/active`);
    return res.data;
};


