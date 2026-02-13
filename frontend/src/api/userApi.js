// src/api/userApi.js
import api from "./axios";

export const userApi = {
    // ADMIN: seznam uživatelů
    getAll: async () => {
        const res = await api.get("/users");
        return res.data;
    },

    // ADMIN: detail uživatele podle ID
    getById: async (id) => {
        const res = await api.get(`/users/${id}`);
        return res.data;
    },

    // ADMIN: updateByAdmin
    updateAdmin: async (id, dto) => {
        const res = await api.put(`/users/${id}`,dto);
        return res.data;
    },

    // ADMIN: reset hesla na výchozí hodnotu
    resetPassword: async (id) => {
        const res = await api.post(`/users/${id}/reset-password`);
        return res.data; // "Heslo resetováno na 'Player123'"
    },

    // ADMIN: aktivace účtu
    activateUser: async (id) => {
        const res = await api.patch(`/users/${id}/activate`);
        return res.data;
    },

    // ADMIN: deaktivace účtu
    deactivateUser: async (id) => {
        const res = await api.patch(`/users/${id}/deactivate`);
        return res.data;
    },


    // === ČÁST PRO PŘIHLÁŠENÉHO UŽIVATELE (/me) ===

    /**
     * Vrátí detail aktuálně přihlášeného uživatele.
     * GET /api/users/me
     */
    getCurrent: async () => {
        const res = await api.get("/users/me");
        return res.data; // AppUserDTO
    },

    /**
     * Aktualizuje údaje aktuálně přihlášeného uživatele.
     * PUT /api/users/me/update
     *
     * @param {Object} dto AppUserDTO
     */
    updateCurrent: async (dto) => {
        const res = await api.put("/users/me/update", dto);
        return res.data; // "Uživatel byl změněn"
    },

    /**
     * Změní heslo aktuálně přihlášeného uživatele.
     * POST /api/users/me/change-password
     *
     * @param {{ oldPassword: string, newPassword: string, newPasswordConfirm: string }} data
     */
    changeMyPassword: async (data) => {
        const res = await api.post("/users/me/change-password", data);
        return res.data; // "Heslo úspěšně změněno"
    },
};
