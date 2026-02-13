// src/api/playerApi.js
import api from "./axios";

/* 
   USER – HRÁČI PŘIHLÁŠENÉHO UŽIVATELE (/me)
 */

/**
 * Načte hráče přihlášeného uživatele
 * GET /api/players/me
 */
export const getMyPlayers = async () => {
    
    
    
    const res = await api.get("/players/me");
    return res.data;
};

/**
 * Vytvoří nového hráče pro přihlášeného uživatele
 * POST /api/players/me
 */
export const createPlayer = async (data) => {
    const res = await api.post("/players/me", data);
    return res.data;
};

/**
 * Nastaví aktuálního hráče
 * POST /api/current-player/{playerId}
 */
export const setCurrentPlayer = async (playerId) => {
    const res = await api.post(`/current-player/${playerId}`);
    return res.data;
};

/**
 * Načte aktuálního hráče
 * GET /api/current-player
 */
export const getCurrentPlayer = async () => {
    const res = await api.get("/current-player");
    return res.data;
};

/**
 * Automatický výběr aktuálního hráče po loginu
 * POST /api/current-player/auto-select
 */
export const autoSelectCurrentPlayer = async () => {
    const res = await api.post("/current-player/auto-select");
    return res.data;
};

/**
 * Aktualizuje údaje aktuálně zvoleného hráče.
 * Mappuje na PUT /api/players/me
 */
export const updateMyCurrentPlayer = async (playerDto) => {
    const res = await api.put("/players/me", playerDto, {
        withCredentials: true,
    });
    return res.data; // PlayerDTO
};

/* 
   ADMIN / MANAGER – GLOBÁLNÍ SPRÁVA HRÁČŮ
 */

/**
 * Načte všechny hráče v systému
 * GET /api/players
 * Role: ADMIN, MANAGER
 */
export const getAllPlayersAdmin = async () => {
    const res = await api.get("/players");
    return res.data;
};

/**
 * Detail hráče podle ID
 * GET /api/players/{id}
 * Role: ADMIN, MANAGER
 */
export const getPlayerById = async (id) => {
    const res = await api.get(`/players/${id}`);
    return res.data;
};

/**
 * Vytvoří hráče globálně
 * POST /api/players
 * Role: ADMIN, MANAGER
 */
export const createPlayerAdmin = async (data) => {
    const res = await api.post("/players", data);
    return res.data;
};

/**
 * Aktualizuje hráče
 * PUT /api/players/{id}
 * Role: ADMIN
 */
export const updatePlayerAdmin = async (id, data) => {
    const res = await api.put(`/players/${id}`, data);
    return res.data;
};

/**
 * Smaže hráče
 * DELETE /api/players/{id}
 * Role: ADMIN
 */
export const deletePlayerAdmin = async (id) => {
    const res = await api.delete(`/players/${id}`);
    return res.data;
};

/**
 * Schválí hráče (APPROVED)
 * PUT /api/players/{id}/approve
 * Role: ADMIN
 */
export const approvePlayerAdmin = async (id) => {
    const res = await api.put(`/players/${id}/approve`);
    return res.data;
};

/**
 * Zamítne hráče (REJECTED)
 * PUT /api/players/{id}/reject
 * Role: ADMIN
 */
export const rejectPlayerAdmin = async (id) => {
    const res = await api.put(`/players/${id}/reject`);
    return res.data;
};

/**
 * Změní přiřazeného uživatele hráče
 * POST /api/players/{playerId}/change-user
 * Role: ADMIN
 */
export const changePlayerUserAdmin = async (playerId, newUserId) => {
    const res = await api.post(`/players/${playerId}/change-user`, {
        newUserId,
    });
    return res.data;
};

/**
 * Historie hráče (ADMIN / MANAGER)
 * GET /api/players/{id}/history
 */
export const getPlayerHistoryAdmin = async (id) => {
    const res = await api.get(`/players/${id}/history`);
    return res.data; // List<PlayerHistoryDTO>
};

/**
 * Historie aktuálního hráče (/me/history)
 * GET /api/players/me/history
 */
export const getMyPlayerHistory = async () => {
    const res = await api.get("/players/me/history");
    return res.data; // List<PlayerHistoryDTO>
};


