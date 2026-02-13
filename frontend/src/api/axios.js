// src/api/axios.js
import axios from "axios";

axios.defaults.withCredentials = true;

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL + "/api",
    withCredentials: true,
});

// ===== Impersonation header (ADMIN) =====
const IMPERSONATION_STORAGE_KEY = "IMPERSONATED_PLAYER_ID";

api.interceptors.request.use(
    (config) => {
        const playerId = localStorage.getItem(IMPERSONATION_STORAGE_KEY);

        // DEBUG – pak smažeme
        console.log("[impersonation] outgoing", config?.url, "playerId=", playerId);

        if (playerId) {
            config.headers = config.headers || {};
            config.headers["X-Impersonate-PlayerId"] = playerId;
            console.log("[impersonation] header set", config.headers["X-Impersonate-PlayerId"]);
        }
        return config;
    },
    (error) => Promise.reject(error)
);

export default api;