// src/components/admin/AdminMatchesTable.jsx
import { useState } from "react";
import AdminMatchCard from "./AdminMatchCard";

const FILTERS = {
    FIRST_UPCOMING: "FIRST_UPCOMING",
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
 * Najde první nadcházející nezrušený zápas.
 */
const getFirstUpcomingMatch = (matches) => {
    if (!Array.isArray(matches) || matches.length === 0) return null;

    const now = new Date();

    const futureMatches = matches
        .map((m) => {
            const d = m.dateTime ? parseDateTime(m.dateTime) : null;
            return { match: m, dateObj: d };
        })
        .filter(
            (x) =>
                x.dateObj &&
                x.dateObj.getTime() > now.getTime() &&
                x.match.matchStatus !== "CANCELED"
        )
        .sort((a, b) => a.dateObj.getTime() - b.dateObj.getTime());

    return futureMatches.length > 0 ? futureMatches[0].match : null;
};

/**
 * Vrátí true, pokud zápas odpovídá zvolenému filtru (mimo FIRST_UPCOMING,
 * ten řešíme zvlášť).
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

/**
 * AdminMatchesTable
 *
 * Komponenta související se zápasy, registracemi a jejich zobrazením.
 *
 * Props:
 * @param {Object} props.matches data vybraného zápasu.
 * @param {boolean} props.loading Příznak, že probíhá načítání dat a UI má zobrazit stav načítání.
 * @param {string} props.error Chybová zpráva určená k zobrazení uživateli.
 * @param {Function} props.onEdit vstupní hodnota komponenty.
 * @param {Function} props.onDelete vstupní hodnota komponenty.
 * @param {Function} props.onCancel callback pro předání akce do nadřazené vrstvy.
 * @param {Function} props.onUnCancel vstupní hodnota komponenty.
 */

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

    // první nadcházející zápas (nezrušený)
    const firstUpcomingMatch = getFirstUpcomingMatch(sortedMatches);

    // počty pro badge u jednotlivých filtrů
    const counts = {
        firstUpcoming: firstUpcomingMatch ? 1 : 0,
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

    const filteredMatches =
        filter === FILTERS.FIRST_UPCOMING
            ? firstUpcomingMatch
                ? [firstUpcomingMatch]
                : []
            : sortedMatches.filter((m) => matchPassesFilter(m, filter));

    const getFilterLabel = (f) => {
        switch (f) {
            case FILTERS.FIRST_UPCOMING:
                return "Nejbližší";
            case FILTERS.FUTURE:
                return "Budoucí";
            case FILTERS.PAST:
                return "Uplynulé";
            case FILTERS.CANCELED:
                return "Zrušené";
            case FILTERS.UNCANCELED:
                return "Obnovené";
            case FILTERS.UPDATED:
                return "Změněné";
            
            case FILTERS.ALL:
            default:
                return "Vše";
        }
    };

    const getFilterCount = (f) => {
        switch (f) {
            case FILTERS.FIRST_UPCOMING:
                return counts.firstUpcoming;
            case FILTERS.FUTURE:
                return counts.future;
            case FILTERS.PAST:
                return counts.past;
            case FILTERS.CANCELED:
                return counts.canceled;
            case FILTERS.UNCANCELED:
                return counts.uncanceled;
            case FILTERS.UPDATED:
                return counts.updated;
            
            case FILTERS.ALL:
            default:
                return counts.all;
        }
    };

    return (
        <div className="d-flex flex-column gap-3">
            {/* ===== FILTR ===== */}
            <div className="mb-2">
                {/* 📱 MOBILE – Dropdown */}
                <div className="d-sm-none">
                    <div className="dropdown w-100">
                        <button
                            className="btn btn-primary dropdown-toggle w-100"
                            type="button"
                            data-bs-toggle="dropdown"
                            aria-expanded="false"
                        >
                            {getFilterLabel(filter)}{" "}
                            <span className="badge bg-light text-dark ms-1">
                                {getFilterCount(filter)}
                            </span>
                        </button>

                        <ul className="dropdown-menu w-100">
                            <li>
                                <button
                                    className="dropdown-item"
                                    onClick={() =>
                                        setFilter(FILTERS.FIRST_UPCOMING)
                                    }
                                >
                                    Nejbližší ({counts.firstUpcoming})
                                </button>
                                <button
                                    className="dropdown-item"
                                    onClick={() => setFilter(FILTERS.ALL)}
                                >
                                    Vše ({counts.all})
                                </button>
                            </li>
                            <li>
                                <button
                                    className="dropdown-item"
                                    onClick={() => setFilter(FILTERS.FUTURE)}
                                >
                                    Budoucí ({counts.future})
                                </button>
                            </li>
                            <li>
                                <button
                                    className="dropdown-item"
                                    onClick={() => setFilter(FILTERS.PAST)}
                                >
                                    Uplynulé ({counts.past})
                                </button>
                            </li>
                            <li>
                                <button
                                    className="dropdown-item"
                                    onClick={() => setFilter(FILTERS.CANCELED)}
                                >
                                    Zrušené ({counts.canceled})
                                </button>
                            </li>
                            <li>
                                <button
                                    className="dropdown-item"
                                    onClick={() => setFilter(FILTERS.UNCANCELED)}
                                >
                                    Obnovené ({counts.uncanceled})
                                </button>
                            </li>
                            <li>
                                <button
                                    className="dropdown-item"
                                    onClick={() => setFilter(FILTERS.UPDATED)}
                                >
                                    Změněné ({counts.updated})
                                </button>
                            </li>
                            <li>
                          
                            </li>
                        </ul>
                    </div>
                </div>

                {/* 💻 DESKTOP – Button group */}
                <div className="d-none d-sm-flex justify-content-center">
                    <div className="btn-group" role="group" aria-label="Filtr zápasů">
                        <button
                            type="button"
                            className={
                                filter === FILTERS.FIRST_UPCOMING
                                    ? "btn btn-primary"
                                    : "btn btn-outline-primary"
                            }
                            onClick={() => setFilter(FILTERS.FIRST_UPCOMING)}
                        >
                            Nejbližší{" "}
                            <span className="badge bg-light text-dark ms-1">
                                {counts.firstUpcoming}
                            </span>
                        </button>
                        
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