// src/pages/PlayerHomePage.jsx
import { Link } from "react-router-dom";
import { useMemo } from "react";

import { useAuth } from "../hooks/useAuth";
import { useCurrentPlayer } from "../hooks/useCurrentPlayer";
import { useMyPlayerStats } from "../hooks/useMyPlayerStats";
import SeasonSelect from "../components/seasons/seasonSelect";
import PlayerStats from "../components/players/PlayerStats";

/**
 * Hráčská domovská stránka.
 *
 * Stránka slouží jako přehled pro přihlášeného uživatele a aktuálně zvoleného hráče.
 * Obsahuje rychlé akce a souhrnné statistiky pro aktuální sezónu.
 */
const PlayerHomePage = () => {
    const { user } = useAuth();
    const { currentPlayer, loading: playerLoading } = useCurrentPlayer();

    const {
        stats,
        loading: statsLoading,
        error: statsError,
        reload: reloadStats,
    } = useMyPlayerStats({ enabled: true });

    const displayName = useMemo(() => {
        const email = user?.email || user?.username || "";
        return email || "Uživatel";
    }, [user]);

    const playerLabel = useMemo(() => {
        if (playerLoading) return "Načítám hráče…";
        if (!currentPlayer) return "Není zvolen aktuální hráč";
        const name =
            currentPlayer?.fullName ||
            currentPlayer?.nickname ||
            "Hráč";
        return name;
    }, [currentPlayer, playerLoading]);

    const formatDateTime = (value) => {
        if (!value) return "—";
        const d =
            value instanceof Date
                ? value
                : new Date(String(value).replace(" ", "T"));
        if (Number.isNaN(d.getTime())) return "—";
        return d.toLocaleString("cs-CZ", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
            hour: "2-digit",
            minute: "2-digit",
        });
    };

    // Poslední úspěšné přihlášení (před aktuálním)
    // Předpoklad: backend posílá user.lastLoginAt jako "minulé" přihlášení.
    const lastLoginLabel = useMemo(() => {
        return formatDateTime(user?.lastLoginAt);
    }, [user]);

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

    return (
        <div className="container py-4">
            {/* Header */}
            <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2 mb-4">
                <div>
                    <h1 className="h3 mb-1">Můj přehled</h1>
                    <p className="text-muted mb-0">
                        Přihlášen:{" "}
                        <span className="fw-semibold">{displayName}</span>{" "}
                        </p>
                    <p className="text-muted mb-0">
                        Hráč:{" "}
                        <span className="fw-semibold">{playerLabel}</span>
                        </p>
                
                    <p className="text-muted mb-0">
                        Poslední přihlášení:{" "}
                        <span className="fw-semibold">
                            {lastLoginLabel}
                        </span>
                    </p>
                </div>

                <div className="d-flex gap-2">
                    <button
                        type="button"
                        className="btn btn-outline-secondary"
                        onClick={reloadStats}
                        disabled={statsLoading}
                        title="Obnoví statistiky"
                    >
                        Obnovit
                    </button>
                </div>
            </div>

            {/* Quick actions */}
            <div className="row g-3 mb-4">
                <ActionCard
                    icon="🏒"
                    title="Zápasy"
                    desc="Přehled zápasů a registrace."
                    to="/app/matches"
                />
                <ActionCard
                    icon="🧑‍🤝‍🧑"
                    title="Moji hráči"
                    desc="Přepnutí aktuálního hráče a správa profilu."
                    to="/app/players"
                />
                <ActionCard
                    icon="⛔"
                    title="Moje absence"
                    desc="Nahlášení neaktivity a omluv."
                    to="/app/my-inactivity"
                />
                <ActionCard
                    icon="🛠️"
                    title="nastavení"
                    desc="Co a jak ti chodí (email/SMS)."
                    to="/app/settings"
                />
            </div>

            {/* Stats */}
            <PlayerStats
                stats={stats}
                loading={statsLoading}
                error={statsError}
                onReload={reloadStats}
            />

            <div className="text-muted small mt-4"></div>
        </div>
    );
};

export default PlayerHomePage;