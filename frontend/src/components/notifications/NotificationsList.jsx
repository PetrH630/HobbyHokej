// src/components/notifications/NotificationsList.jsx
import { useEffect, useMemo, useState } from "react";
import NotificationCard from "./NotificationCard";
import {
    fetchRecentNotifications,
    markNotificationAsRead,
    markAllNotificationsAsRead,
} from "../../api/notificationsApi";
import { useNotificationBadge } from "../../hooks/useNotificationBadge";
import {
    Stars, // Nové
    EnvelopeExclamation, // Nepřečtené
    EnvelopeOpen, // Přečtené
} from "react-bootstrap-icons";

/**
 * Komponenta se seznamem notifikací.
 *
 * Načítá:
 * - lastLoginAt z NotificationBadgeContext (badge),
 * - poslední notifikace přes /notifications/recent.
 *
 * Filtry:
 * - Nové       = nepřečtené notifikace od posledního přihlášení
 * - Nepřečtené = všechny nepřečtené notifikace z recent seznamu
 * - Přečtené   = všechny přečtené notifikace z recent seznamu
 */
const NotificationsList = () => {
    const [notifications, setNotifications] = useState([]);
    const [activeFilter, setActiveFilter] = useState("NEW"); // NEW | UNREAD | READ
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // 🔔 badge z contextu – badge.lastLoginAt + refetch pro zvonek
    const { badge, refetch: refetchBadge } = useNotificationBadge() || {};

    // helper na převod na Date
    const toDate = (value) => {
        if (!value) return null;
        if (value instanceof Date) return value;
        if (typeof value === "string") {
            const safe = value.includes("T")
                ? value
                : value.replace(" ", "T");
            const d = new Date(safe);
            return Number.isNaN(d.getTime()) ? null : d;
        }
        return null;
    };

    // lastLoginAt bereme přímo z badge DTO
    const lastLoginDate = useMemo(
        () => toDate(badge?.lastLoginAt ?? null),
        [badge]
    );

    // inicializační načtení seznamu notifikací
    useEffect(() => {
        const load = async () => {
            try {
                setLoading(true);
                setError(null);

                const recent = await fetchRecentNotifications(200);
                setNotifications(recent || []);
            } catch (e) {
                console.error(e);
                setError("Nepodařilo se načíst notifikace.");
            } finally {
                setLoading(false);
            }
        };

        load();
    }, []);

    // 🔹 Detekce, zda je notifikace přečtená
    const isNotificationRead = (n) => {
        if (n.read === true) return true;
        if (n.read === "true") return true;
        if (n.readAt != null) return true;
        return false;
    };

    // 🔹 Notifikace vznikla po posledním přihlášení
    const isCreatedAfterLastLogin = (n) => {
        if (!lastLoginDate) return false;
        const created = toDate(
            n.createdAt || n.timestamp || n.created
        );
        if (!created) return false;
        return created >= lastLoginDate;
    };

    // 🔹 Počty pro badge na stránce
    const counts = useMemo(() => {
        let newCount = 0;
        let unreadCount = 0;
        let readCount = 0;

        notifications.forEach((n) => {
            const isRead = isNotificationRead(n);
            const createdAfter = isCreatedAfterLastLogin(n);

            if (isRead) {
                readCount++;
            } else {
                unreadCount++;

                if (lastLoginDate) {
                    if (createdAfter) {
                        newCount++;
                    }
                } else {
                    // když neznáme lastLogin, bereme všechny nepřečtené jako "nové"
                    newCount++;
                }
            }
        });

        return {
            NEW: newCount,
            UNREAD: unreadCount,
            READ: readCount,
        };
    }, [notifications, lastLoginDate]);

    // 🔹 Filtrovaný seznam
    const filteredNotifications = useMemo(() => {
        switch (activeFilter) {
            case "NEW":
                if (lastLoginDate) {
                    return notifications.filter(
                        (n) =>
                            !isNotificationRead(n) &&
                            isCreatedAfterLastLogin(n)
                    );
                }
                return notifications.filter(
                    (n) => !isNotificationRead(n)
                );

            case "UNREAD":
                return notifications.filter(
                    (n) => !isNotificationRead(n)
                );

            case "READ":
                return notifications.filter((n) =>
                    isNotificationRead(n)
                );

            default:
                return notifications;
        }
    }, [notifications, activeFilter, lastLoginDate]);

    // 🔹 Označit jednu jako přečtenou
    const handleMarkOneAsRead = async (id) => {
        try {
            await markNotificationAsRead(id);
            setNotifications((prev) =>
                prev.map((n) =>
                    n.id === id
                        ? {
                            ...n,
                            read: true,
                            readAt:
                                n.readAt ??
                                new Date().toISOString(),
                        }
                        : n
                )
            );
            if (typeof refetchBadge === "function") {
                refetchBadge();
            }
        } catch (e) {
            console.error(e);
        }
    };

    // 🔹 Označit všechny jako přečtené
    const handleMarkAllAsRead = async () => {
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
            if (typeof refetchBadge === "function") {
                refetchBadge();
            }
        } catch (e) {
            console.error(e);
        }
    };

    if (loading) {
        return (
            <div className="card">
                <div className="card-body">
                    <p className="mb-0">
                        Načítám notifikace…
                    </p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="card">
                <div className="card-body">
                    <p className="mb-0 text-danger">
                        {error}
                    </p>
                </div>
            </div>
        );
    }

    return (
        <div className="card">
            <div className="card-header d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-2">
                <div className="d-flex flex-column flex-sm-row align-items-sm-center gap-2">
                    <span className="fw-semibold">Notifikace</span>

                    {/* Filtrovací tlačítka + badge s počty */}
                    <div
                        className="btn-group btn-group-sm"
                        role="group"
                        aria-label="Filtr notifikací"
                    >
                        {/* Nové */}
                        <button
                            type="button"
                            className={
                                "btn btn-outline-secondary d-flex align-items-center gap-1" +
                                (activeFilter === "NEW"
                                    ? " active"
                                    : "")
                            }
                            onClick={() =>
                                setActiveFilter("NEW")
                            }
                            title="Nové notifikace (nepřečtené od posledního přihlášení)"
                        >
                            <Stars size={14} />
                            <span className="d-none d-sm-inline">
                                Nové
                            </span>
                            <span
                                className={
                                    "badge " +
                                    (activeFilter === "NEW"
                                        ? "bg-light text-dark"
                                        : "bg-secondary")
                                }
                            >
                                {counts.NEW}
                            </span>
                        </button>

                        {/* Nepřečtené */}
                        <button
                            type="button"
                            className={
                                "btn btn-outline-secondary d-flex align-items-center gap-1" +
                                (activeFilter === "UNREAD"
                                    ? " active"
                                    : "")
                            }
                            onClick={() =>
                                setActiveFilter("UNREAD")
                            }
                            title="Všechny nepřečtené notifikace"
                        >
                            <EnvelopeExclamation size={14} />
                            <span className="d-none d-sm-inline">
                                Nepřečtené
                            </span>
                            <span
                                className={
                                    "badge " +
                                    (activeFilter === "UNREAD"
                                        ? "bg-light text-dark"
                                        : "bg-secondary")
                                }
                            >
                                {counts.UNREAD}
                            </span>
                        </button>

                        {/* Přečtené */}
                        <button
                            type="button"
                            className={
                                "btn btn-outline-secondary d-flex align-items-center gap-1" +
                                (activeFilter === "READ"
                                    ? " active"
                                    : "")
                            }
                            onClick={() =>
                                setActiveFilter("READ")
                            }
                            title="Všechny přečtené notifikace"
                        >
                            <EnvelopeOpen size={14} />
                            <span className="d-none d-sm-inline">
                                Přečtené
                            </span>
                            <span
                                className={
                                    "badge " +
                                    (activeFilter === "READ"
                                        ? "bg-light text-dark"
                                        : "bg-secondary")
                                }
                            >
                                {counts.READ}
                            </span>
                        </button>
                    </div>
                </div>

                {/* Označit vše jako přečtené – podle všech nepřečtených v recent seznamu */}
                {counts.UNREAD > 0 && (
                    <button
                        type="button"
                        className="btn btn-sm btn-outline-primary"
                        onClick={handleMarkAllAsRead}
                        title="Označit všechny notifikace jako přečtené"
                    >
                        Označit vše jako přečtené (
                        {counts.UNREAD})
                    </button>
                )}
            </div>

            <div className="card-body">
                {filteredNotifications.length === 0 ? (
                    <p className="mb-0 text-muted">
                        {notifications.length === 0
                            ? "Nemáte žádné notifikace."
                            : "Žádné notifikace pro zvolený filtr."}
                    </p>
                ) : (
                    filteredNotifications.map((n) => (
                        <NotificationCard
                            key={n.id}
                            notification={n}
                            onMarkRead={handleMarkOneAsRead}
                        />
                    ))
                )}
            </div>
        </div>
    );
};

export default NotificationsList;