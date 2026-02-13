// src/api/modeApi.js
import api from "./axios";

/**
 * Načte režim aplikace (demo / produkce) z backendu.
 *
 * Volá: GET {baseURL}/public/app-mode
 * → s baseURL = VITE_API_URL + "/api"
 * tedy typicky: http://localhost:8080/api/public/app-mode
 */
export const fetchAppMode = async () => {
    const response = await api.get("/public/app-mode");

    console.log("[modeApi] /public/app-mode response.data:", response.data);

    return response.data; // očekáváme objekt { demoMode: true/false }
};
