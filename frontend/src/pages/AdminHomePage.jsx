import { Link, useNavigate } from "react-router-dom";
import { useMemo, useState } from "react";

import { useAllPlayersAdmin } from "../hooks/useAllPlayersAdmin";
import { useAllMatchesAdmin } from "../hooks/useAllMatchesAdmin";
import { useAllSeasonsAdmin } from "../hooks/useAllSeasonsAdmin";
import { useAllUsersAdmin } from "../hooks/useAllUsersAdmin";
import { useAuth } from "../hooks/useAuth";

const AdminHomePage = () => {
    const navigate = useNavigate();
    const { user } = useAuth();

    const isAdmin =
        user?.roles?.includes("ADMIN") || user?.role === "ADMIN";
    const isManager =
        !isAdmin &&
        (user?.roles?.includes("MANAGER") || user?.role === "MANAGER");

    const [showManagerInfo, setShowManagerInfo] = useState(isManager);

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

    const playersCount = players?.length ?? 0;
    const matchesCount = matches?.length ?? 0;
    const seasonsCount = seasons?.length ?? 0;
    const usersCount = users?.length ?? 0;

    const reloadAll = () => {
        reloadPlayers?.();
        reloadMatches?.();
        reloadSeasons?.();
        reloadUsers?.();
    };

    const combinedError =
        playersError || matchesError || seasonsError || usersError || "";

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
                label: "Uživatelé",
                value: renderValue(usersLoading, usersCount),
                helper: "Registrovaní v systému",
            },
            {
                label: "Hráči",
                value: renderValue(playersLoading, playersCount),
                helper: "Včetně čekajících",
            },
            {
                label: "Sezóny",
                value: renderValue(seasonsLoading, seasonsCount),
                helper: "V databázi",
            },
            {
                label: "Zápasy",
                value: renderValue(matchesLoading, matchesCount),
                helper: "Aktuální sezóna",
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

    const lastActivities = [
        { time: "Dnes 12:41", text: "Změněn čas zápasu (ukázka).", type: "change" },
        { time: "Dnes 10:05", text: "Schválen hráč (ukázka).", type: "approve" },
        { time: "Včera 19:22", text: "Vytvořena sezóna (ukázka).", type: "create" },
    ];

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

    const Badge = ({ text }) => (
        <span className="badge text-bg-secondary">{text}</span>
    );

    const ActivityIcon = ({ type }) => {
        const map = { change: "✏️", approve: "✅", create: "➕" };
        return <span className="me-2">{map[type] || "📝"}</span>;
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

                <div className="d-flex gap-2">
                    <button
                        className="btn btn-outline-secondary"
                        type="button"
                        onClick={reloadAll}
                        disabled={
                            playersLoading || matchesLoading || seasonsLoading || usersLoading
                        }
                        title="Obnoví přehledové údaje z databáze"
                    >
                        Obnovit
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
                            <div className="d-flex justify-content-between align-items-center">
                                <div className="fw-semibold">Nadcházející zápasy</div>
                                <Link
                                    to="/app/admin/matches"
                                    className="btn btn-sm btn-outline-primary"
                                >
                                    Správa zápasů
                                </Link>
                            </div>
                        </div>

                        <div className="table-responsive">
                            <table className="table table-hover mb-0 align-middle">
                                <thead className="table-light">
                                    <tr>
                                        <th>Datum</th>
                                        <th>Soupeř</th>
                                        <th>Místo</th>
                                        <th>Status</th>
                                        <th className="text-end">Akce</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {matchesLoading ? (
                                        <tr>
                                            <td colSpan={5} className="text-muted py-4">
                                                Načítám zápasy…
                                            </td>
                                        </tr>
                                    ) : upcomingMatches.length === 0 ? (
                                        <tr>
                                            <td colSpan={5} className="text-muted py-4">
                                                Žádné nadcházející zápasy.
                                            </td>
                                        </tr>
                                    ) : (
                                        upcomingMatches.map((m) => {
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
                                                <tr key={id ?? `${opponent}-${dateObj?.toISOString()}`}>
                                                    <td className="text-nowrap">
                                                        {formatDateTime(dateObj)}
                                                    </td>
                                                    <td>{opponent}</td>
                                                    <td className="text-nowrap">{place}</td>
                                                    <td>
                                                        <Badge text={status} />
                                                    </td>
                                                    <td className="text-end">
                                                        {id ? (
                                                            <button
                                                                type="button"
                                                                className="btn btn-sm btn-outline-primary"
                                                                disabled
                                                                title="Zatím není implementováno"
                                                            >
                                                                Detail
                                                            </button>
                                                        ) : (
                                                            <span className="text-muted small">—</span>
                                                        )}
                                                    </td>
                                                </tr>
                                            );
                                        })
                                    )}
                                </tbody>
                            </table>
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
                                    Poslední aktivity - zatím neimplementováno
                                </div>
                                <div className="card-body">
                                    <ul className="list-group list-group-flush">
                                        {lastActivities.map((a, idx) => (
                                            <li key={idx} className="list-group-item px-0">
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
                                            Produkce / Demo dle nastavení 
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
                                        <div className="fw-semibold mb-1">Bude se doplňovat</div>
                                        <ul className="mb-0 small text-muted">
                                            <li>graf registrací na zápasy (7/30 dní)</li>
                                            
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
        </div>
    );
};

export default AdminHomePage;
