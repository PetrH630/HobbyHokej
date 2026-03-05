/**
 * demoNotificationsApi
 *
 * Modul pro komunikaci s backendovým REST API.
 * Obsahuje pomocné funkce pro volání endpointů a sjednocení práce s axios klientem.
 */

// src/api/demoNotificationsApi.js
import api from "./axios";

export const getDemoNotifications = () => {
    return api.get("/demo/notifications");
};
