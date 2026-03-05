/**
 * adminPlayersApi
 *
 * Modul pro komunikaci s backendovým REST API.
 * Obsahuje pomocné funkce pro volání endpointů a sjednocení práce s axios klientem.
 */

// src/api/adminPlayersApi.js
import api from "./axios";

export const fetchAllPlayersAdmin = async () => {
    return api.get("/players"); 
};
