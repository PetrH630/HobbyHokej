/**
 * axios
 *
 * Modul pro komunikaci s backendovým REST API.
 * Obsahuje pomocné funkce pro volání endpointů a sjednocení práce s axios klientem.
 */

import axios from "axios";

const api = axios.create({
    baseURL: "/api",
    withCredentials: true,
});

export default api;