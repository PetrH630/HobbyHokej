// src/context/NotificationBadgeContext.jsx
import { createContext, useCallback, useEffect, useState } from "react";
import {
    fetchNotificationBadge,
    fetchNotificationsSinceLastLogin,
} from "../api/notificationsApi";

export const NotificationBadgeContext = createContext({
    badge: null,
    loading: false,
    error: null,
    refetch: () => { },
});

// pomocná funkce – stejné chování jako v NotificationsList/useNotifications
const isNotificationRead = (n) => {
    if (!n) return false;
    if (n.read === true) return true;
    if (n.read === "true") return true;
    if (n.readAt != null) return true;
    return false;
};

export const NotificationBadgeProvider = ({ children }) => {
    const [badge, setBadge] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const loadBadge = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            // 1) badge DTO (kvůli lastLoginAt a případně jiným věcem)
            // 2) seznam notifikací od posledního přihlášení
            const [badgeDto, sinceLastLogin] = await Promise.all([
                fetchNotificationBadge(),
                fetchNotificationsSinceLastLogin(),
            ]);

            // spočítáme počet NEPŘEČTENÝCH notifikací od posledního přihlášení
            let computedUnread = 0;

            if (Array.isArray(sinceLastLogin)) {
                computedUnread = sinceLastLogin.filter(
                    (n) => !isNotificationRead(n)
                ).length;
            }

            // fallback: pokud z nějakého důvodu seznam není, zkusíme hodnotu z backendu
            const unreadCountSinceLastLogin =
                computedUnread > 0
                    ? computedUnread
                    : badgeDto?.unreadCountSinceLastLogin ?? 0;

            setBadge({
                ...badgeDto,
                unreadCountSinceLastLogin,
            });
        } catch (err) {
            console.error("Chyba při načítání notification badge:", err);
            setError("Nepodařilo se načíst notifikace.");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadBadge();
    }, [loadBadge]);

    return (
        <NotificationBadgeContext.Provider
            value={{
                badge,
                loading,
                error,
                refetch: loadBadge,
            }}
        >
            {children}
        </NotificationBadgeContext.Provider>
    );
};