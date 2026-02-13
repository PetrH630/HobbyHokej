// src/api/matchApi.js
import api from "./axios";

/* =========================================================
   Hráč – zápasy pro currentPlayer
   ========================================================= */

/**
 * Nadcházející zápasy (přehled) pro aktuálního hráče.
 * GET /api/matches/me/upcoming-overview
 */
export const getMyUpcomingMatchesOverview = async () => {
    const res = await api.get("/matches/me/upcoming-overview", {
        withCredentials: true,
    });
    return res.data; // List<MatchOverviewDTO>
};

/**
 * Uplynulé zápasy (přehled) pro aktuálního hráče.
 * GET /api/matches/me/all-passed
 */
export const getMyPassedMatchesOverview = async () => {
    const res = await api.get("/matches/me/all-passed", {
        withCredentials: true,
    });
    return res.data; // List<MatchOverviewDTO>
};

/**
 * Detail zápasu z pohledu hráče.
 * GET /api/matches/{id}/detail
 */
export const getMatchDetail = async (id) => {
    const res = await api.get(`/matches/${id}/detail`, {
        withCredentials: true,
    });
    return res.data; // MatchDetailDTO
};

/* 
   ADMIN / MANAGER – globální správa zápasů
*/

/**
 * Vrátí všechny zápasy v systému (v aktuální sezóně).
 * GET /api/matches
 * Role: ADMIN, MANAGER
 */
export const getAllMatchesAdmin = async () => {
    const res = await api.get("/matches");
    return res.data; // List<MatchDTO>
};

/**
 * Vrátí všechny nadcházející zápasy.
 * GET /api/matches/upcoming
 * Role: ADMIN, MANAGER
 */
export const getUpcomingMatchesAdmin = async () => {
    const res = await api.get("/matches/upcoming");
    return res.data; // List<MatchDTO>
};

/**
 * Vrátí všechny proběhlé zápasy.
 * GET /api/matches/past
 * Role: ADMIN, MANAGER
 */
export const getPastMatchesAdmin = async () => {
    const res = await api.get("/matches/past");
    return res.data; // List<MatchDTO>
};

/**
 * Vytvoří nový zápas.
 * POST /api/matches
 * Role: ADMIN
 */
export const createMatchAdmin = async (data) => {
    const res = await api.post("/matches", data);
    return res.data; // MatchDTO
};

/**
 * Aktualizuje existující zápas.
 * PUT /api/matches/{id}
 * Role: ADMIN, MANAGER
 */
export const updateMatchAdmin = async (id, data) => {
    const res = await api.put(`/matches/${id}`, data);
    return res.data; // MatchDTO
};

/**
 * Smaže zápas.
 * DELETE /api/matches/{id}
 * Role: ADMIN
 */
export const deleteMatchAdmin = async (id) => {
    const res = await api.delete(`/matches/${id}`);
    return res.data; // SuccessResponseDTO
};

/**
 * Zruší zápas s důvodem.
 * PATCH /api/matches/{matchId}/cancel?reason=...
 * Role: ADMIN, MANAGER
 */
export const cancelMatchAdmin = async (matchId, reason) => {
    const res = await api.patch(`/matches/${matchId}/cancel`, null, {
        params: { reason },
    });
    return res.data; // SuccessResponseDTO
};

/**
 * Obnoví dříve zrušený zápas.
 * PATCH /api/matches/{matchId}/uncancel
 * Role: ADMIN, MANAGER
 */
export const unCancelMatchAdmin = async (matchId) => {
    const res = await api.patch(`/matches/${matchId}/uncancel`);
    return res.data; // SuccessResponseDTO
};

/**
 * Historie zápasu.
 * GET /api/matches/{id}/history
 * Role: ADMIN, MANAGER
 */
export const getMatchHistoryAdmin = async (id) => {
    const res = await api.get(`/matches/${id}/history`);
    return res.data; // List<MatchHistoryDTO>
};

/**
 * Zápasy dostupné pro konkrétního hráče.
 * GET /api/matches/available-for-player/{playerId}
 * Role: ADMIN, MANAGER
 */
export const getAvailableMatchesForPlayerAdmin = async (playerId) => {
    const res = await api.get(`/matches/available-for-player/${playerId}`);
    return res.data; // List<MatchDTO>
};
