// src/api/notificationsApi.js
import api from "./axios";

/**
 * Načítá badge s počtem nepřečtených notifikací od posledního přihlášení.
 *
 * Používá endpoint GET /api/notifications/badge.
 *
 * @returns {Promise<Object>} NotificationBadgeDTO
 */
export const fetchNotificationBadge = async () => {
    const response = await api.get("/notifications/badge");
    return response.data;
};

/**
 * Načítá notifikace od posledního přihlášení.
 *
 * Používá endpoint GET /api/notifications/since-last-login.
 *
 * @returns {Promise<Array>} Pole NotificationDTO
 */
export const fetchNotificationsSinceLastLogin = async () => {
    const response = await api.get("/notifications/since-last-login");
    return response.data;
};

/**
 * Načítá poslední notifikace aktuálního uživatele.
 *
 * Používá endpoint GET /api/notifications/recent.
 *
 * @param {number} [limit=50] Maximální počet notifikací.
 * @returns {Promise<Array>} Pole NotificationDTO
 */
export const fetchRecentNotifications = async (limit = 50) => {
    const response = await api.get("/notifications/recent", {
        params: { limit },
    });
    return response.data;
};

/**
 * Označí konkrétní notifikaci jako přečtenou.
 *
 * Používá endpoint POST /api/notifications/{id}/read.
 *
 * @param {number} id ID notifikace.
 * @returns {Promise<void>}
 */
export const markNotificationAsRead = async (id) => {
    await api.post(`/notifications/${id}/read`);
};

/**
 * Označí všechny notifikace aktuálního uživatele jako přečtené.
 *
 * Používá endpoint POST /api/notifications/read-all.
 *
 * @returns {Promise<void>}
 */
export const markAllNotificationsAsRead = async () => {
    await api.post("/notifications/read-all");
};

/**
 * Načítá všechny notifikace v systému pro administrátorský přehled.
 *
 * Používá endpoint GET /api/notifications/admin/all.
 *
 * @param {number} [limit=500] Maximální počet notifikací.
 * @returns {Promise<Array>} Pole NotificationDTO
 */
export const fetchAllNotificationsAdmin = async (limit = 500) => {
    const response = await api.get("/notifications/admin/all", {
        params: { limit },
    });
    return response.data;
};

/**
 * Načítá možné cíle (uživatelé/hráči) pro speciální notifikaci.
 *
 * Používá endpoint GET /api/notifications/admin/special/targets.
 *
 * @returns {Promise<Array>} Pole SpecialNotificationTargetDTO
 */
export const fetchSpecialNotificationTargets = async () => {
    const response = await api.get("/notifications/admin/special/targets");
    return response.data;
};

/**
 * Odesílá speciální zprávu vybraným uživatelům / hráčům.
 *
 * Používá endpoint POST /api/notifications/admin/special.
 *
 * Vstupní objekt odpovídá SpecialNotificationRequestDTO:
 * {
 *   title: string,
 *   message: string,
 *   sendEmail: boolean,
 *   sendSms: boolean,
 *   targets: Array<{ userId: number, playerId: number | null }>
 * }
 *
 * V produkci backend vrací HTTP 204 No Content.
 * V DEMO módu backend vrací DemoNotificationsDTO (např. zachycené e-maily/SMS).
 *
 * @param {Object} payload SpecialNotificationRequestDTO
 * @returns {Promise<null|Object>} null v produkci, nebo DemoNotificationsDTO v DEMO režimu.
 */
export const sendSpecialNotification = async (payload) => {
    const response = await api.post("/notifications/admin/special", payload);

    // PRODUKCE: 204 → žádná data
    if (response.status === 204) {
        return null;
    }

    // DEMO mód: 200 OK + DemoNotificationsDTO v body
    return response.data ?? null;
};