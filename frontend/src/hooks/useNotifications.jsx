// src/hooks/useNotifications.js
import { useCallback, useEffect, useState } from "react";
import {
    fetchNotificationsSinceLastLogin,
    fetchRecentNotifications,
    fetchAllNotificationsAdmin,
    markNotificationAsRead,
    markAllNotificationsAsRead,
} from "../api/notificationsApi";

/**
 * Hook pro práci se seznamem notifikací.
 *
 * Podporované režimy:
 * - "sinceLastLogin" – notifikace od posledního přihlášení (default),
 * - "recent" – poslední notifikace dle limitu,
 * - "adminAll" – všechny notifikace pro admin/manager přehled.
 *
 * @param {Object} options Konfigurační volby.
 * @param {"sinceLastLogin"|"recent"|"adminAll"} [options.mode="sinceLastLogin"] Režim načítání.
 * @param {number} [options.limit] Maximální počet záznamů (používá se pro recent/adminAll).
 */
export const useNotifications = ({ mode = "sinceLastLogin", limit } = {}) => {
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    
    const normalizeData = (data) => {
        if (!data) return [];
        if (Array.isArray(data)) return data;
        if (Array.isArray(data.content)) return data.content;
        return [];
    };

    const load = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            let data;

            console.log("[useNotifications] load start, mode =", mode, "limit =", limit);

            if (mode === "recent") {
                data = await fetchRecentNotifications(limit);
            } else if (mode === "adminAll") {
                data = await fetchAllNotificationsAdmin(limit);
            } else {
                data = await fetchNotificationsSinceLastLogin();
            }

            console.log("[useNotifications] raw data from API:", data);

            const normalized = normalizeData(data);

            console.log("[useNotifications] normalized notifications:", normalized);

            setNotifications(normalized);
        } catch (err) {
            console.error("Chyba při načítání notifikací:", err);
            setError("Nepodařilo se načíst notifikace.");
            setNotifications([]);
        } finally {
            setLoading(false);
        }
    }, [mode, limit]);

    useEffect(() => {
        load();
    }, [load]);

    const handleMarkOneAsRead = useCallback(async (id) => {
        try {
            await markNotificationAsRead(id);
            setNotifications((prev) =>
                prev.map((n) =>
                    n.id === id
                        ? {
                            ...n,
                            read: true,
                            readAt: n.readAt ?? new Date().toISOString(),
                        }
                        : n
                )
            );
        } catch (err) {
            console.error("Chyba při označování notifikace jako přečtené:", err);
        }
    }, []);

    const handleMarkAllAsRead = useCallback(async () => {
        try {
            await markAllNotificationsAsRead();
            const nowIso = new Date().toISOString();
            setNotifications((prev) =>
                prev.map((n) => ({
                    ...n,
                    read: true,
                    readAt: n.readAt ?? nowIso,
                }))
            );
        } catch (err) {
            console.error("Chyba při označování všech notifikací jako přečtených:", err);
        }
    }, []);

    return {
        notifications,
        loading,
        error,
        refetch: load,
        markOneAsRead: handleMarkOneAsRead,
        markAllAsRead: handleMarkAllAsRead,
    };
};