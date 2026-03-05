// src/pages/AdminHomePage.jsx
import { Link, useNavigate } from "react-router-dom";
import { useMemo, useState } from "react";

import { useAllPlayersAdmin } from "../hooks/useAllPlayersAdmin";
import { useAllMatchesAdmin } from "../hooks/useAllMatchesAdmin";
import { useAllSeasonsAdmin } from "../hooks/useAllSeasonsAdmin";
import { useAllUsersAdmin } from "../hooks/useAllUsersAdmin";
import { useAuth } from "../hooks/useAuth";
import { useNotifications } from "../hooks/useNotifications";
import AdminSpecialNotificationModal from "../components/notifications/AdminSpecialNotificationModal";

/**
 * AdminHomePage
 *
 * zápas registrace – Bootstrap modal.
 *
 * Vedlejší efekty:
 * - může provádět navigaci v aplikaci
 *
 * @param {Object} props vstupní hodnoty komponenty
 */
const AdminHomePage = () => {
    const navigate = useNavigate();
    const { user } = useAuth();

    const isAdmin =
        user?.roles?.includes("ADMIN") || user?.role === "ADMIN";
    const isManager =
        !isAdmin &&
        (user?.roles?.includes("MANAGER") || user?.role === "MANAGER");

    const [showManagerInfo, setShowManagerInfo] = useState(isManager);

    // 👇 nový state pro modal speciálních zpráv
    const [showSpecialModal, setShowSpecialModal] = useState(false);

    const {
        players,
        loading: playersLoading,
        error: playersError,
        reload: reloadPlayers,
    } = useAllPlayersAdmin();

    const {
        matches,
        loading: matchesLoading,
        error: matchesError,
        reload: reloadMatches,
    } = useAllMatchesAdmin();

    const {
        seasons,
        loading: seasonsLoading,
        error: seasonsError,
        reload: reloadSeasons,
    } = useAllSeasonsAdmin();

    const {
        users,
        loading: usersLoading,
        error: usersError,
        reload: reloadUsers,
    } = useAllUsersAdmin();

    // Notifikace pro admin/manager přehled
    const {
        notifications,
        loading: notificationsLoading,
        error: notificationsError,
    } = useNotifications({ mode: "adminAll", limit: 200 });

    const playersCount = players?.length ?? 0;
    const matchesCount = matches?.length ?? 0;
    const seasonsCount = seasons?.length ?? 0;
    const usersCount = users?.length ?? 0;

    const reloadAll = () => {
        reloadPlayers?.();
        reloadMatches?.();
        reloadSeasons?.();
        reloadUsers?.();
        // případné refetch notifikací řeší useNotifications přes změnu klíčů / externí volání
    };

    const combinedError =
        playersError ||
        matchesError ||
        seasonsError ||
        usersError ||
        notificationsError ||
        "";

    const renderValue = (loading, value) => (loading ? "…" : value);

    // --- helpers for match date parsing/formatting ---
    const parseMatchDate = (m) => {
        const raw =
            m?.startTime ||
            m?.dateTime ||
            m?.matchTime ||
            m?.matchDateTime ||
            m?.date ||
            m?.time;

        if (!raw) return null;

        const d = new Date(String(raw).replace(" ", "T"));
        return Number.isNaN(d.getTime()) ? null : d;
    };

    const formatDateTime = (d) => {
        if (!d) return "—";
        return d.toLocaleString("cs-CZ", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
            hour: "2-digit",
            minute: "2-digit",
        });
    };

    // --- status badge pro zápasy ---
    const getMatchStatusBadge = (statusRaw) => {
        const status = String(statusRaw || "").toUpperCase();

        const map = {
            UPDATED: {
                label: "Změněný",
                className: "text-bg-warning",
            },
            CANCELED: {
                label: "Zrušený",
                className: "text-bg-danger",
            },
            UNCANCELED: {
                label: "Obnovený",
                className: "text-bg-success",
            },
        };

        const resolved = map[status] || {
            label: "Plánovaný",
            className: "text-bg-secondary",
        };

        return (
            <span className={`badge ${resolved.className}`}>
                {resolved.label}
            </span>
        );
    };

    const upcomingMatches = useMemo(() => {
        const now = new Date();
        const list = Array.isArray(matches) ? matches : [];

        return list
            .map((m) => {
                const dateObj = parseMatchDate(m);
                return { ...m, _dateObj: dateObj };
            })
            .filter((m) => m._dateObj && m._dateObj.getTime() > now.getTime())
            .sort((a, b) => a._dateObj.getTime() - b._dateObj.getTime())
            .slice(0, 5);
    }, [matches]);

    const stats = useMemo(
        () => [
            {
                label: "Uživatelů",
                value: renderValue(usersLoading, usersCount),
                helper: "Registrovaní v systému",
            },
            {
                label: "Hráčů",
                value: renderValue(playersLoading, playersCount),
                helper: "Včetně čekajících",
            },
            {
                label: "Sezón",
                value: renderValue(seasonsLoading, seasonsCount),
                helper: "V databázi",
            },
            {
                label: "Zápasů",
                value: renderValue(matchesLoading, matchesCount),
                helper: "v aktuální sezóně",
            },
        ],
        [
            usersLoading,
            usersCount,
            playersLoading,
            playersCount,
            seasonsLoading,
            seasonsCount,
            matchesLoading,
            matchesCount,
        ]
    );

    // --- helpers pro aktivity (notifikace) ---
    const mapNotificationTypeToActivityType = (type) => {
        if (!type) return "info";
        const t = String(type).toLowerCase();

        if (t.includes("time_changed") || t.includes("updated") || t.includes("changed")) {
            return "change";
        }
        if (t.includes("approved") || t.includes("activated") || t.includes("uncanceled")) {
            return "approve";
        }
        if (t.includes("created") || t.includes("new") || t.includes("registered")) {
            return "create";
        }
        if (t.includes("canceled") || t.includes("rejected") || t.includes("deleted")) {
            return "cancel";
        }

        return "info";
    };

    // časové okno pro sloučení duplicitních notifikací (stejný typ + text)
    const ACTIVITY_TIME_WINDOW_MS = 1000; // 1 sekunda

    const makeActivityKey = (n) => {
        const type = String(n.type || "").toLowerCase();
        const text = (n.messageShort || n.message || n.title || "")
            .trim()
            .toLowerCase();

        // Záměrně jen typ + text, aby se nesledoval konkrétní příjemce
        return [type, text].join("|");
    };

    const lastActivities = useMemo(() => {
        if (!Array.isArray(notifications)) return [];

        // Normalizace – připravíme key + dateObj
        const normalized = notifications
            .map((n) => {
                if (!n) return null;

                const dateObj = n.createdAt
                    ? new Date(String(n.createdAt).replace(" ", "T"))
                    : null;

                if (!dateObj || Number.isNaN(dateObj.getTime())) {
                    return null;
                }

                return {
                    notif: n,
                    dateObj,
                    key: makeActivityKey(n),
                };
            })
            .filter(Boolean);

        // Nejprve seřadit od nejnovějších
        normalized.sort(
            (a, b) => b.dateObj.getTime() - a.dateObj.getTime()
        );

        const unique = [];

        for (const item of normalized) {
            const { notif, dateObj, key } = item;

            // Zjistit, jestli už v unique existuje "stejná" aktivita
            // (stejný key a časový rozdíl <= ACTIVITY_TIME_WINDOW_MS)
            const alreadyExists = unique.some((u) => {
                if (u.key !== key) return false;
                const diff = Math.abs(
                    u.dateObj.getTime() - dateObj.getTime()
                );
                return diff <= ACTIVITY_TIME_WINDOW_MS;
            });

            if (alreadyExists) {
                // jen další exemplář stejné vlny notifikací (víc hráčů)
                continue;
            }

            unique.push(item);

            // stačí prvních 10 unikátních aktivit
            if (unique.length >= 10) {
                break;
            }
        }

        return unique.map(({ notif, dateObj }) => ({
            id: notif.id,
            time: formatDateTime(dateObj),
            text:
                notif.messageShort ||
                notif.message ||
                notif.title ||
                "Systémová notifikace",
            type: mapNotificationTypeToActivityType(notif.type),
        }));
    }, [notifications]);

    const ActionCard = ({ title, desc, to, icon }) => (
        <div className="col-12 col-md-6 col-xl-3">
            <div className="card h-100 shadow-sm">
                <div className="card-body d-flex flex-column">
                    <div className="d-flex align-items-center gap-2 mb-2">
                        <span className="fs-4">{icon}</span>
                        <h5 className="card-title mb-0">{title}</h5>
                    </div>
                    <p className="text-muted small mb-3">{desc}</p>
                    <div className="mt-auto">
                        <Link to={to} className="btn btn-outline-primary w-100">
                            Otevřít
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );

    const ActivityIcon = ({ type }) => {
        const map = {
            change: "✏️",
            approve: "✅",
            create: "➕",
            cancel: "❌",
            info: "📝",
        };
        return <span className="me-2">{map[type] || map.info}</span>;
    };

    const handleCloseManagerInfo = () => {
        setShowManagerInfo(false);
        navigate("/app/admin/players");
    };

    return (
        <div className="container py-4">
            {/* Header */}
            <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2 mb-4">
                <div>
                    <h1 className="h3 mb-1">Správa</h1>
                    <p className="text-muted mb-0">
                        Přehled systému a rychlé akce pro správu.
                    </p>
                </div>

                {/* 👇 upraveno: na malém zařízení pod sebou, od md vedle sebe */}
                <div className="d-flex flex-column flex-md-row gap-2">
                    <button
                        className="btn btn-outline-secondary"
                        type="button"
                        onClick={reloadAll}
                        disabled={
                            playersLoading ||
                            matchesLoading ||
                            seasonsLoading ||
                            usersLoading
                        }
                        title="Obnoví přehledové údaje z databáze"
                    >
                        Obnovit
                    </button>

                    {/* 👇 nové tlačítko vedle Obnovit */}
                    <button
                        className="btn btn-primary"
                        type="button"
                        onClick={() => setShowSpecialModal(true)}
                        title="Odešle speciální zprávu vybraným uživatelům / hráčům"
                    >
                        Poslat speciální zprávu
                    </button>
                </div>
            </div>

            {/* Manager info + redirect on close */}
            {showManagerInfo && (
                <div className="alert alert-info d-flex justify-content-between align-items-start" role="alert">
                    <div className="me-3">
                        <div className="fw-semibold mb-1">Režim správce (MANAGER)</div>
                        <div className="small">
                            Tato stránka je sdílená pro Admin i Manager. Po zavření této zprávy
                            bude otevřena Správa hráčů.
                        </div>
                    </div>

                    <button
                        type="button"
                        className="btn-close"
                        aria-label="Zavřít"
                        onClick={handleCloseManagerInfo}
                    />
                </div>
            )}

            {/* Error */}
            {combinedError && (
                <div className="alert alert-danger" role="alert">
                    {combinedError}
                </div>
            )}

            {/* Quick actions */}
            <div className="row g-3 mb-4">
                <ActionCard
                    icon="📅"
                    title="Sezóny"
                    desc="Správa sezón, aktivní sezóna a základní nastavení."
                    to="/app/admin/seasons"
                />
                <ActionCard
                    icon="🏒"
                    title="Zápasy"
                    desc="Vytváření zápasů, úpravy času a místa, publikace."
                    to="/app/admin/matches"
                />
                <ActionCard
                    icon="👤"
                    title="Uživatelé"
                    desc="Role, aktivace účtů, reset hesel a oprávnění."
                    to="/app/admin/users"
                />
                <ActionCard
                    icon="🧑‍🤝‍🧑"
                    title="Hráči"
                    desc="Schvalování hráčů, správa statusů a profilů."
                    to="/app/admin/players"
                />
            </div>

            {/* Stats */}
            <div className="row g-3 mb-4">
                {stats.map((s) => (
                    <div className="col-12 col-md-6 col-xl-3" key={s.label}>
                        <div className="card h-100 shadow-sm">
                            <div className="card-body">
                                <div className="d-flex justify-content-between align-items-start">
                                    <div>
                                        <div className="text-muted small">{s.label}</div>
                                        <div className="display-6 mb-0">{s.value}</div>
                                    </div>
                                    <span className="badge text-bg-light border">
                                        Přehled
                                    </span>
                                </div>
                                <div className="text-muted small mt-2">{s.helper}</div>
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {/* Main content */}
            <div className="row g-3">
                {/* Upcoming matches */}
                <div className="col-12 col-xl-7">
                    <div className="card shadow-sm h-100">
                        <div className="card-header bg-white">
                            <div className="d-flex flex-column flex-md-row justify-content-md-between align-items-md-center gap-2">
                                <div className="fw-semibold">Nadcházející zápasy</div>
                                <Link
                                    to="/app/admin/matches"
                                    className="btn btn-sm btn-outline-primary"
                                >
                                    Správa zápasů
                                </Link>
                            </div>
                        </div>

                        <div className="card-body">
                            {matchesLoading ? (
                                <div className="text-muted py-2">Načítám zápasy…</div>
                            ) : upcomingMatches.length === 0 ? (
                                <div className="text-muted py-2">Žádné nadcházející zápasy.</div>
                            ) : (
                                <div className="d-flex flex-column gap-3">
                                    {upcomingMatches.map((m) => {
                                        const dateObj = m._dateObj;
                                        const opponent =
                                            m?.opponentName ||
                                            m?.opponent ||
                                            m?.rival ||
                                            m?.title ||
                                            "—";
                                        const place =
                                            m?.place ||
                                            m?.location ||
                                            m?.stadium ||
                                            "—";
                                        const status =
                                            m?.status ||
                                            m?.matchStatus ||
                                            "Plánováno";
                                        const id = m?.id;

                                        return (
                                            <div
                                                key={id ?? `${opponent}-${dateObj?.toISOString()}`}
                                                className="card border-0 shadow-sm"
                                            >
                                                <div className="card-body">
                                                    <div className="d-flex flex-column flex-md-row justify-content-between gap-2">
                                                        <div>
                                                            <div className="fw-semibold">
                                                                {formatDateTime(dateObj)}{" - "}
                                                                {opponent}
                                                            </div>
                                                        </div>
                                                        <div>
                                                            <div className="text-nowrap">{place}</div>
                                                        </div>

                                                        <div className="ms-md-auto">
                                                            {getMatchStatusBadge(status)}
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                        </div>

                        <div className="card-body border-top">
                            <div className="text-muted small">
                                Zobrazuje se prvních 5 budoucích zápasů dle data.
                            </div>
                        </div>
                    </div>
                </div>

                {/* Activity + System info */}
                <div className="col-12 col-xl-5">
                    <div className="row g-3">
                        <div className="col-12">
                            <div className="card shadow-sm">
                                <div className="card-header bg-white fw-semibold">
                                    <div className="d-flex flex-column flex-md-row justify-content-md-between align-items-md-center gap-2">
                                        Poslední aktivity
                                        <Link
                                            to="/app/admin/notifications"
                                            className="btn btn-sm btn-outline-primary"
                                        >
                                            Správa aktivit
                                        </Link>
                                    </div>
                                </div>
                                <div className="card-body">
                                    {notificationsLoading ? (
                                        <div className="text-muted py-2">
                                            Načítám poslední aktivity…
                                        </div>
                                    ) : lastActivities.length === 0 ? (
                                        <div className="text-muted py-2">
                                            Žádné notifikace k zobrazení.
                                        </div>
                                    ) : (
                                        <>
                                            <ul className="list-group list-group-flush">
                                                {lastActivities.map((a) => (
                                                    <li
                                                        key={a.id ?? a.time}
                                                        className="list-group-item px-0"
                                                    >
                                                        <div className="d-flex justify-content-between">
                                                            <div>
                                                                <ActivityIcon type={a.type} />
                                                                <span>{a.text}</span>
                                                            </div>
                                                            <div className="text-muted small text-nowrap ms-3">
                                                                {a.time}
                                                            </div>
                                                        </div>
                                                    </li>
                                                ))}
                                            </ul>

                                            <div className="mt-3">
                                                <button
                                                    type="button"
                                                    className="btn btn-sm btn-outline-primary"
                                                    disabled
                                                    title="Audit log zatím není implementován"
                                                >
                                                    Zobrazit audit log
                                                </button>
                                            </div>
                                        </>
                                    )}
                                </div>
                            </div>
                        </div>

                        <div className="col-12">
                            <div className="card shadow-sm">
                                <div className="card-header bg-white fw-semibold">Systém</div>
                                <div className="card-body">
                                    <div className="d-flex justify-content-between mb-2">
                                        <span className="text-muted">Prostředí</span>
                                        <span className="fw-semibold">
                                            Demo dle nastavení
                                        </span>
                                    </div>
                                    <div className="d-flex justify-content-between mb-2">
                                        <span className="text-muted">Notifikace</span>
                                        <span className="fw-semibold">
                                            Email / SMS dle konfigurace
                                        </span>
                                    </div>
                                    <div className="d-flex justify-content-between">
                                        <span className="text-muted">Migrace DB</span>
                                        <span className="fw-semibold">Flyway aktivní</span>
                                    </div>

                                    <hr />

                                    <div className="alert alert-light border mb-0">
                                        <div className="fw-semibold mb-1">Doplnit...</div>
                                        <ul className="mb-0 small text-muted">
                                            <li></li>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="text-muted small mt-4">
                Počty i nadcházející zápasy jsou načteny z databáze přes admin hooky.
            </div>

            {/* 👇 modal pro speciální zprávu */}
            <AdminSpecialNotificationModal
                show={showSpecialModal}
                onClose={() => setShowSpecialModal(false)}
                onSent={() => {
                    // pokud budeš chtít po odeslání něco refreshnout, můžeš doplnit
                    // např. reload notifikací nebo jen toast
                }}
            />
        </div>
    );
};

export default AdminHomePage;