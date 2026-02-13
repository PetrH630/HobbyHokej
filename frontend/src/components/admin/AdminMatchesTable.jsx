// src/components/admin/AdminMatchesTable.jsx
import { useState } from "react";
import AdminMatchCard from "./AdminMatchCard";

const FILTERS = {
    ALL: "ALL",
    FUTURE: "FUTURE",
    PAST: "PAST",
    CANCELED: "CANCELED",
    UNCANCELED: "UNCANCELED",
    UPDATED: "UPDATED",
};

const parseDateTime = (dt) => {
    if (!dt) return null;
    const safe = dt.includes("T") ? dt : dt.replace(" ", "T");
    const d = new Date(safe);
    return Number.isNaN(d.getTime()) ? null : d;
};

const isPastMatch = (match) => {
    if (!match || !match.dateTime) return false;
    const d = parseDateTime(match.dateTime);
    if (!d) return false;
    const now = new Date();
    return d < now;
};

/**
 * Vrátí true, pokud zápas odpovídá zvolenému filtru.
 */
const matchPassesFilter = (match, filter) => {
    const status = match.matchStatus; 
    const past = isPastMatch(match);


    switch (filter) {
        case FILTERS.FUTURE:
            return !past;

        case FILTERS.PAST:
            return past;

        case FILTERS.CANCELED:
            return status === "CANCELED";

        case FILTERS.UNCANCELED:
            return status === "UNCANCELED";

        case FILTERS.UPDATED:
            return status === "UPDATED";

        case FILTERS.ALL:
        default:
            return true;
    }
};

const AdminMatchesTable = ({
    matches,
    loading,
    error,
    onEdit,
    onDelete,
    onCancel,
    onUnCancel,
}) => {
    const [filter, setFilter] = useState(FILTERS.ALL);

    if (loading) {
        return <p>Načítám zápasy…</p>;
    }

    if (error) {
        return (
            <div className="alert alert-danger" role="alert">
                {error}
            </div>
        );
    }

    if (!matches || matches.length === 0) {
        return <p>V systému zatím nejsou žádné zápasy.</p>;
    }

    // Řazení – nejnovější nahoře (dle matchNumber, fallback dle data)
    const sortedMatches = matches.slice().sort((a, b) => {
        if (a.matchNumber != null && b.matchNumber != null) {
            return b.matchNumber - a.matchNumber;
        }

        if (!a.dateTime || !b.dateTime) return 0;

        const safeA = a.dateTime.replace(" ", "T");
        const safeB = b.dateTime.replace(" ", "T");
        return new Date(safeB) - new Date(safeA);
    });

    // počty pro badge u jednotlivých filtrů
    const counts = {
        all: sortedMatches.length,
        future: sortedMatches.filter((m) =>
            matchPassesFilter(m, FILTERS.FUTURE)
        ).length,
        past: sortedMatches.filter((m) =>
            matchPassesFilter(m, FILTERS.PAST)
        ).length,
        canceled: sortedMatches.filter((m) =>
            matchPassesFilter(m, FILTERS.CANCELED)
        ).length,
        uncanceled: sortedMatches.filter((m) =>
            matchPassesFilter(m, FILTERS.UNCANCELED)
        ).length,
        updated: sortedMatches.filter((m) =>
            matchPassesFilter(m, FILTERS.UPDATED)
        ).length,
    };

    const filteredMatches = sortedMatches.filter((m) =>
        matchPassesFilter(m, filter)
    );

    return (
        <div className="d-flex flex-column gap-3">
            {/* Filtrovací tlačítka – podobně jako u PastMatches */}
            <div className="d-flex justify-content-center mb-3">
                <div
                    className="btn-group"
                    role="group"
                    aria-label="Filtr zápasů"
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
                        Vše{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.all}
                        </span>
                    </button>

                    <button
                        type="button"
                        className={
                            filter === FILTERS.FUTURE
                                ? "btn btn-primary"
                                : "btn btn-outline-primary"
                        }
                        onClick={() => setFilter(FILTERS.FUTURE)}
                    >
                        Budoucí{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.future}
                        </span>
                    </button>

                    <button
                        type="button"
                        className={
                            filter === FILTERS.PAST
                                ? "btn btn-primary"
                                : "btn btn-outline-primary"
                        }
                        onClick={() => setFilter(FILTERS.PAST)}
                    >
                        Uplynulé{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.past}
                        </span>
                    </button>

                    <button
                        type="button"
                        className={
                            filter === FILTERS.CANCELED
                                ? "btn btn-primary"
                                : "btn btn-outline-primary"
                        }
                        onClick={() => setFilter(FILTERS.CANCELED)}
                    >
                        Zrušené{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.canceled}
                        </span>
                    </button>

                    <button
                        type="button"
                        className={
                            filter === FILTERS.UNCANCELED
                                ? "btn btn-primary"
                                : "btn btn-outline-primary"
                        }
                        onClick={() => setFilter(FILTERS.UNCANCELED)}
                    >
                        Obnovené{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.uncanceled}
                        </span>
                    </button>

                    <button
                        type="button"
                        className={
                            filter === FILTERS.UPDATED
                                ? "btn btn-primary"
                                : "btn btn-outline-primary"
                        }
                        onClick={() => setFilter(FILTERS.UPDATED)}
                    >
                        Změněné{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.updated}
                        </span>
                    </button>
                </div>
            </div>

            {/* Info, když nic neodpovídá filtru */}
            {filteredMatches.length === 0 && (
                <p className="text-center mb-3">
                    Pro zvolený filtr nemáte žádné zápasy.
                </p>
            )}

            {filteredMatches.map((match) => (
                <AdminMatchCard
                    key={match.id}
                    match={match}
                    onEdit={onEdit}
                    onDelete={onDelete}
                    onCancel={onCancel}
                    onUnCancel={onUnCancel}
                />
            ))}
        </div>
    );
};

export default AdminMatchesTable;
