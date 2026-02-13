// src/components/admin/AdminPlayersTable.jsx
import { useState } from "react";
import AdminPlayerCard from "./AdminPlayerCard";
import { usePlayerInactivityPeriodsAdmin } from "../../hooks/usePlayerInactivityPeriodsAdmin";

const FILTERS = {
    ALL: "ALL",
    APPROVED: "APPROVED",
    PENDING: "PENDING",
    REJECTED: "REJECTED",
    INACTIVE: "INACTIVE",
};

// Pomocná funkce – filtr podle statusu + neaktivity
const playerPassesFilter = (player, isInactive, filter) => {
    const status = player.playerStatus;

    switch (filter) {
        case FILTERS.APPROVED:
            // aktivní = schválený a právě není v neaktivitě
            return status === "APPROVED" && !isInactive;

        case FILTERS.INACTIVE:
            // filtr čistě na aktuálně neaktivní hráče
            return isInactive === true;

        case FILTERS.PENDING:
            return status === "PENDING" || status === "NEW";

        case FILTERS.REJECTED:
            return status === "REJECTED";

        case FILTERS.ALL:
        default:
            return true;
    }
};

const AdminPlayersTable = ({
    players,
    loading,
    error,
    onApprove,
    onReject,
    onEdit,
    onDelete,
    onChangeUser,
}) => {
    const [filter, setFilter] = useState(FILTERS.ALL);

    // načtení všech období neaktivity
    const {
        periods,
        loading: inactivityLoading,
        error: inactivityError,
        reload: reloadInactivity,       // 👈 DŮLEŽITÉ: přidáme reload z hooku
    } = usePlayerInactivityPeriodsAdmin();

    // když se načítají hráči nebo neaktivita → loader
    if (loading || inactivityLoading) {
        return <p>Načítám hráče…</p>;
    }

    if (error) {
        return (
            <div className="alert alert-danger" role="alert">
                {error}
            </div>
        );
    }

    if (inactivityError) {
        return (
            <div className="alert alert-danger" role="alert">
                {inactivityError}
            </div>
        );
    }

    if (!players || players.length === 0) {
        return <p>V systému zatím nejsou žádní hráči.</p>;
    }

    // připravíme mapu playerId -> isInactiveNow (abychom to nepočítali dokola)
    const now = new Date();
    const inactivityMap = new Map();

    if (periods && periods.length > 0) {
        periods.forEach((p) => {
            const playerId = p.playerId;
            if (!playerId) return;

            const rawFrom = p.inactiveFrom;
            const rawTo = p.inactiveTo;
            if (!rawFrom || !rawTo) return;

            // LocalDateTime může být "2026-02-10T10:00:00" nebo "2026-02-10 10:00:00"
            const from = new Date(
                typeof rawFrom === "string"
                    ? rawFrom.replace(" ", "T")
                    : rawFrom
            );
            const to = new Date(
                typeof rawTo === "string"
                    ? rawTo.replace(" ", "T")
                    : rawTo
            );

            if (isNaN(from.getTime()) || isNaN(to.getTime())) return;

            const isInThisInterval = from <= now && to >= now;

            if (isInThisInterval) {
                inactivityMap.set(playerId, true);
            }
        });
    }

    const isPlayerInactiveNow = (playerId) =>
        inactivityMap.get(playerId) === true;

    // základní řazení podle příjmení
    const sortedPlayers = players
        .slice()
        .sort((a, b) =>
            (a.surname || "").localeCompare(b.surname || "", "cs", {
                sensitivity: "base",
            })
        );

    // počty pro badge u jednotlivých filtrů
    const counts = {
        all: sortedPlayers.length,
        active: sortedPlayers.filter((p) =>
            playerPassesFilter(p, isPlayerInactiveNow(p.id), FILTERS.APPROVED)
        ).length,
        inactive: sortedPlayers.filter((p) =>
            playerPassesFilter(p, isPlayerInactiveNow(p.id), FILTERS.INACTIVE)
        ).length,
        pending: sortedPlayers.filter((p) =>
            playerPassesFilter(p, isPlayerInactiveNow(p.id), FILTERS.PENDING)
        ).length,
        rejected: sortedPlayers.filter((p) =>
            playerPassesFilter(p, isPlayerInactiveNow(p.id), FILTERS.REJECTED)
        ).length,
    };

    const filteredPlayers = sortedPlayers.filter((p) =>
        playerPassesFilter(p, isPlayerInactiveNow(p.id), filter)
    );

    return (
        <div className="d-flex flex-column gap-3">
            {/* Filtrovací tlačítka – stejné chování jako u PastMatches */}
            <div className="d-flex justify-content-center mb-3">
                <div
                    className="btn-group"
                    role="group"
                    aria-label="Filtr hráčů"
                >
                    <button
                        type="button"
                        className={
                            filter === FILTERS.ALL
                                ? "btn btn-primary"
                                : "btn btn-outline-primary"
                        }
                        onClick={() => setFilter(FILTERS.ALL)}
                    >
                        Všichni{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.all}
                        </span>
                    </button>

                    <button
                        type="button"
                        className={
                            filter === FILTERS.APPROVED
                                ? "btn btn-primary"
                                : "btn btn-outline-primary"
                        }
                        onClick={() => setFilter(FILTERS.APPROVED)}
                    >
                        Aktivní{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.active}
                        </span>
                    </button>

                    <button
                        type="button"
                        className={
                            filter === FILTERS.PENDING
                                ? "btn btn-primary"
                                : "btn btn-outline-primary"
                        }
                        onClick={() => setFilter(FILTERS.PENDING)}
                    >
                        Čeká na schválení{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.pending}
                        </span>
                    </button>

                    <button
                        type="button"
                        className={
                            filter === FILTERS.INACTIVE
                                ? "btn btn-primary"
                                : "btn btn-outline-primary"
                        }
                        onClick={() => setFilter(FILTERS.INACTIVE)}
                    >
                        Neaktivní{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.inactive}
                        </span>
                    </button>

                    <button
                        type="button"
                        className={
                            filter === FILTERS.REJECTED
                                ? "btn btn-primary"
                                : "btn btn-outline-primary"
                        }
                        onClick={() => setFilter(FILTERS.REJECTED)}
                    >
                        Zamítnutí{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.rejected}
                        </span>
                    </button>
                </div>
            </div>

            {/* Info, když filtr nic nevrátí */}
            {filteredPlayers.length === 0 && (
                <p className="text-center mb-3">
                    Pro zvolený filtr nejsou žádní hráči.
                </p>
            )}

            {filteredPlayers.map((player) => (
                <AdminPlayerCard
                    key={player.id}
                    player={player}
                    isInactive={isPlayerInactiveNow(player.id)}
                    onApprove={onApprove}
                    onReject={onReject}
                    onEdit={onEdit}
                    onDelete={onDelete}
                    onChangeUser={onChangeUser}
                    onInactivityChanged={reloadInactivity}   // 👈 TADY – po změně znovu načteme
                />
            ))}
        </div>
    );
};

export default AdminPlayersTable;
