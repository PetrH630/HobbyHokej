// src/api/userApi.js
import axios from 'axios';

const api = axios.create({
    baseURL: '/api',
});

export const userApi = {
    getAll: async () => {
        const res = await api.get('/users');
        return res.data;
    },

    getById: async (id) => {
        const res = await api.get(`/users/${id}`);
        return res.data;
    },

    resetPassword: async (id) => {
        const res = await api.post(`/users/${id}/reset-password`);
        return res.data; // "Heslo resetováno na 'Player123'"
    },

    activate: async (id) => {
        const res = await api.patch(`/users/${id}/activate`);
        return res.data;
    },

    deactivate: async (id) => {
        const res = await api.patch(`/users/${id}/deactivate`);
        return res.data;
    },
};
