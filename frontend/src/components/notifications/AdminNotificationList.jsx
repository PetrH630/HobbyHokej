// src/components/notifications/AdminNotificationsList.jsx
import { useEffect, useMemo, useState } from "react";
import AdminNotificationCard from "./AdminNotificationCard";
import { fetchAllNotificationsAdmin } from "../../api/notificationsApi";
import {
    Bell,                // Vše
    EnvelopeExclamation, // Nepřečtené
    EnvelopeOpen,        // Přečtené
} from "react-bootstrap-icons";

/**
 * Admin/Manager přehled všech notifikací v systému.
 *
 * Vlastnosti:
 * - Načítá notifikace přes /api/notifications/admin/all.
 * - Jen read-only (admin nesahá na readAt uživatelů).
 * - Filtry: Vše, Nepřečtené, Přečtené.
 *
 * Nově:
 * - Notifikace se seskupují do "událostí" (stejný typ + text + časové okno).
 * - V jedné kartě je pak seznam uživatelů a jejich stav (přečteno/nepřečteno).
 */
const AdminNotificationsList = () => {
    const [notifications, setNotifications] = useState([]);
    const [activeFilter, setActiveFilter] = useState("ALL"); // ALL | UNREAD | READ
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const isNotificationRead = (n) => {
        if (n.read === true) return true;
        if (n.read === "true") return true;
        if (n.readAt != null) return true;
        return false;
    };

    // časové okno pro sloučení duplicitních notifikací do jedné události
    const GROUP_TIME_WINDOW_MS = 1000; // 1 sekunda

    /**
     * Klíč pro seskupení notifikací do "událostí".
     * Používá se typ + text (krátká / dlouhá zpráva).
     */
    const makeGroupKey = (n) => {
        const type = String(n.type || "").toLowerCase();
        const text = (
            n.messageShort ||
            n.messageFull ||
            n.message ||
            n.title ||
            ""
        )
            .trim()
            .toLowerCase();

        if (!type && !text) {
            return null;
        }

        return `${type}|${text}`;
    };

    useEffect(() => {
        const load = async () => {
            try {
                setLoading(true);
                setError(null);

                const all = await fetchAllNotificationsAdmin(500);
                setNotifications(all || []);
            } catch (e) {
                console.error(e);
                setError("Nepodařilo se načíst systémové notifikace.");
            } finally {
                setLoading(false);
            }
        };

        load();
    }, []);

    /**
     * Seskupené notifikace do "událostí".
     *
     * Každá skupina má tvar:
     * {
     *   key: string,
     *   createdAt: string | null,
     *   createdMs: number | null,
     *   important: boolean,
     *   notifications: NotificationDTO[]
     * }
     */
    const groupedNotifications = useMemo(() => {
        if (!Array.isArray(notifications) || notifications.length === 0) {
            return [];
        }

        // Normalizace – doplnění key a timestampu
        const normalized = notifications
            .map((n) => {
                const key = makeGroupKey(n);
                if (!key) return null;

                const rawCreated = n.createdAt ?? null;
                const createdDate = rawCreated
                    ? new Date(String(rawCreated).replace(" ", "T"))
                    : null;
                const createdMs =
                    createdDate && !Number.isNaN(createdDate.getTime())
                        ? createdDate.getTime()
                        : null;

                return {
                    key,
                    notification: n,
                    createdAt: rawCreated,
                    createdMs,
                };
            })
            .filter(Boolean);

        // Seřadíme od nejnovějších (podle createdMs, fallback 0)
        normalized.sort(
            (a, b) => (b.createdMs ?? 0) - (a.createdMs ?? 0)
        );

        const groups = [];

        for (const item of normalized) {
            const { key, createdMs, createdAt, notification } = item;

            // Zkusíme najít existující skupinu se stejným klíčem a v časovém okně
            let existingGroup = groups.find((g) => {
                if (g.key !== key) return false;

                // Pokud u obou máme timestamp, hlídat časové okno
                if (g.createdMs != null && createdMs != null) {
                    const diff = Math.abs(g.createdMs - createdMs);
                    return diff <= GROUP_TIME_WINDOW_MS;
                }

                // Pokud timestamp chybí, seskupujeme jen podle klíče
                return true;
            });

            if (!existingGroup) {
                existingGroup = {
                    key,
                    createdAt,
                    createdMs,
                    important: !!notification.important,
                    notifications: [],
                };
                groups.push(existingGroup);
            } else {
                // Uložíme nejnovější čas a "important", pokud některá z notifikací je důležitá
                if (
                    createdMs != null &&
                    (existingGroup.createdMs == null ||
                        createdMs > existingGroup.createdMs)
                ) {
                    existingGroup.createdMs = createdMs;
                    existingGroup.createdAt = createdAt;
                }

                if (notification.important) {
                    existingGroup.important = true;
                }
            }

            existingGroup.notifications.push(notification);
        }

        return groups;
    }, [notifications]);

    const counts = useMemo(() => {
        let unreadGroups = 0;
        let readGroups = 0;

        groupedNotifications.forEach((group) => {
            const hasUnread = group.notifications.some(
                (n) => !isNotificationRead(n)
            );
            const allRead =
                group.notifications.length > 0 &&
                group.notifications.every((n) => isNotificationRead(n));

            if (hasUnread) unreadGroups++;
            if (allRead) readGroups++;
        });

        return {
            ALL: groupedNotifications.length,
            UNREAD: unreadGroups,
            READ: readGroups,
        };
    }, [groupedNotifications]);

    const filteredNotifications = useMemo(() => {
        switch (activeFilter) {
            case "UNREAD":
                return groupedNotifications.filter((group) =>
                    group.notifications.some((n) => !isNotificationRead(n))
                );
            case "READ":
                return groupedNotifications.filter(
                    (group) =>
                        group.notifications.length > 0 &&
                        group.notifications.every((n) =>
                            isNotificationRead(n)
                        )
                );
            case "ALL":
            default:
                return groupedNotifications;
        }
    }, [groupedNotifications, activeFilter]);

    if (loading) {
        return (
            <div className="card">
                <div className="card-body">
                    <p className="mb-0">Načítám systémové notifikace…</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="card">
                <div className="card-body">
                    <p className="mb-0 text-danger">{error}</p>
                </div>
            </div>
        );
    }

    return (
        <div className="card">
            <div className="card-header d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-2">
                <div className="d-flex flex-column flex-sm-row align-items-sm-center gap-2">
                    <span className="fw-semibold">
                        Systémové notifikace (admin)
                    </span>

                    <div
                        className="btn-group btn-group-sm"
                        role="group"
                        aria-label="Filtr systémových notifikací"
                    >
                        {/* Vše */}
                        <button
                            type="button"
                            className={
                                "btn btn-outline-secondary d-flex align-items-center gap-1" +
                                (activeFilter === "ALL" ? " active" : "")
                            }
                            onClick={() => setActiveFilter("ALL")}
                            title="Zobrazit všechny systémové notifikace"
                        >
                            <Bell size={14} />
                            <span className="d-none d-sm-inline">
                                Vše
                            </span>
                            <span
                                className={
                                    "badge " +
                                    (activeFilter === "ALL"
                                        ? "bg-light text-dark"
                                        : "bg-secondary")
                                }
                            >
                                {counts.ALL}
                            </span>
                        </button>

                        {/* Nepřečtené */}
                        <button
                            type="button"
                            className={
                                "btn btn-outline-secondary d-flex align-items-center gap-1" +
                                (activeFilter === "UNREAD" ? " active" : "")
                            }
                            onClick={() => setActiveFilter("UNREAD")}
                            title="Zobrazit pouze nepřečtené notifikace"
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
                                (activeFilter === "READ" ? " active" : "")
                            }
                            onClick={() => setActiveFilter("READ")}
                            title="Zobrazit pouze přečtené notifikace"
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
            </div>

            <div className="card-body">
                {filteredNotifications.length === 0 ? (
                    <p className="mb-0 text-muted">
                        {groupedNotifications.length === 0
                            ? "V systému nejsou žádné notifikace."
                            : "Žádné notifikace pro zvolený filtr."}
                    </p>
                ) : (
                    filteredNotifications.map((group) => (
                        <AdminNotificationCard
                            key={
                                group.notifications[0]?.id ??
                                group.key
                            }
                            group={group}
                        />
                    ))
                )}
            </div>
        </div>
    );
};

export default AdminNotificationsList;