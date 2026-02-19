import axios from "axios";

/**
 * Centrální Axios instance pro komunikaci s backendem.
 *
 * Režimy:
 * 1) Lokální vývoj:
 *    VITE_API_BASE_URL=http://localhost:8080
 *
 * 2) Produkce – jeden server (nginx proxy):
 *    Vždy používá relativní "/api"
 *
 * 3) Produkce – oddělený backend:
 *    Lze upravit podle potřeby
 */

const isProd = import.meta.env.PROD;

const apiBase = isProd
    ? "/api"  // 🔥 na VPS vždy relativní cesta
    : (import.meta.env.VITE_API_BASE_URL
        ? `${import.meta.env.VITE_API_BASE_URL.replace(/\/$/, "")}/api`
        : "/api");

const api = axios.create({
    baseURL: apiBase,
    withCredentials: true,
});

export default api;
