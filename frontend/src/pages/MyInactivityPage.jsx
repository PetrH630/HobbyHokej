// src/pages/MyInactivityPage.jsx
import { useMemo, useState } from "react";
import { useMyInactivity } from "../hooks/useMyInactivity";
import { useCurrentPlayer } from "../hooks/useCurrentPlayer";

const normalizeDate = (value) => {
    if (!value) return null;

    const raw =
        typeof value === "string"
            ? value.includes("T")
                ? value
                : value.replace(" ", "T")
            : value;

    const d = new Date(raw);
    return Number.isNaN(d.getTime()) ? null : d;
};

const formatDateTime = (value) => {
    const d = normalizeDate(value);
    if (!d) return "-";
    return d.toLocaleString("cs-CZ");
};

const FILTERS = {
    ALL: "ALL",
    CURRENT: "CURRENT",
    NON_CURRENT: "NON_CURRENT",
};

const MyInactivityPage = () => {
    const { periods, loading, error, reload } = useMyInactivity();
    const { currentPlayer } = useCurrentPlayer();

    const [filter, setFilter] = useState(FILTERS.ALL);

    const filteredPeriods = useMemo(() => {
        switch (filter) {
            case FILTERS.CURRENT:
                return periods.filter((p) => p.isCurrent);
            case FILTERS.NON_CURRENT:
                return periods.filter((p) => !p.isCurrent);
            case FILTERS.ALL:
            default:
                return periods;
        }
    }, [periods, filter]);

    const counts = useMemo(() => {
        const all = periods.length;
        const current = periods.filter((p) => p.isCurrent).length;
        const nonCurrent = all - current;

        return { all, current, nonCurrent };
    }, [periods]);

    const getFilterLabel = (f) => {
        switch (f) {
            case FILTERS.CURRENT:
                return "Aktuálně neaktivní";
            case FILTERS.NON_CURRENT:
                return "Neaktuální";
            case FILTERS.ALL:
            default:
                return "Všechna";
        }
    };

    const getFilterCount = (f) => {
        switch (f) {
            case FILTERS.CURRENT:
                return counts.current;
            case FILTERS.NON_CURRENT:
                return counts.nonCurrent;
            case FILTERS.ALL:
            default:
                return counts.all;
        }
    };

    if (loading) {
        return <p>Načítám informace o neaktivitě…</p>;
    }

    if (error) {
        return (
            <div className="alert alert-danger">
                {error}
                <div className="mt-2">
                    <button
                        type="button"
                        className="btn btn-sm btn-outline-light"
                        onClick={reload}
                    >
                        Zkusit znovu načíst
                    </button>
                </div>
            </div>
        );
    }

    const playerName =
        currentPlayer?.surname || currentPlayer?.name
            ? `${currentPlayer?.surname?.toUpperCase() || ""} ${currentPlayer?.name || ""
                }`.trim()
            : "Hráč";

    return (
        <div className="container py-3">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h1 className="h2 mb-2">
                    Moje období neaktivity
                    {playerName && (
                        <span className="d-block h6 text-muted mt-3 mb-0">
                            {playerName}
                        </span>
                    )}
                </h1>
                <button
                    type="button"
                    className="btn btn-outline-secondary btn-sm"
                    onClick={reload}
                >
                    Znovu načíst
                </button>
            </div>

            <p className="text-muted mb-3">
                Celkový počet období neaktivity:{" "}
                <strong>{periods.length}</strong>
            </p>

            {/* ===== FILTR ===== */}
            <div className="mb-4">

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
                                    onClick={() => setFilter(FILTERS.ALL)}
                                >
                                    Všechna ({counts.all})
                                </button>
                            </li>
                            <li>
                                <button
                                    className="dropdown-item"
                                    onClick={() =>
                                        setFilter(FILTERS.CURRENT)
                                    }
                                >
                                    Aktuálně neaktivní ({counts.current})
                                </button>
                            </li>
                            <li>
                                <button
                                    className="dropdown-item"
                                    onClick={() =>
                                        setFilter(FILTERS.NON_CURRENT)
                                    }
                                >
                                    Neaktuální ({counts.nonCurrent})
                                </button>
                            </li>
                        </ul>
                    </div>
                </div>

                {/* 💻 DESKTOP – Button group */}
                <div className="d-none d-sm-flex justify-content-center">
                    <div
                        className="btn-group"
                        role="group"
                        aria-label="Filtr období neaktivity"
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
                            Všechna{" "}
                            <span className="badge bg-light text-dark ms-1">
                                {counts.all}
                            </span>
                        </button>

                        <button
                            type="button"
                            className={
                                filter === FILTERS.CURRENT
                                    ? "btn btn-primary"
                                    : "btn btn-outline-primary"
                            }
                            onClick={() =>
                                setFilter(FILTERS.CURRENT)
                            }
                        >
                            Aktuálně neaktivní{" "}
                            <span className="badge bg-light text-dark ms-1">
                                {counts.current}
                            </span>
                        </button>

                        <button
                            type="button"
                            className={
                                filter === FILTERS.NON_CURRENT
                                    ? "btn btn-primary"
                                    : "btn btn-outline-primary"
                            }
                            onClick={() =>
                                setFilter(FILTERS.NON_CURRENT)
                            }
                        >
                            Neaktuální{" "}
                            <span className="badge bg-light text-dark ms-1">
                                {counts.nonCurrent}
                            </span>
                        </button>
                    </div>
                </div>
            </div>

            {filteredPeriods.length === 0 && (
                <p>Pro zvolený filtr nejsou žádná období neaktivity.</p>
            )}

            <div className="d-flex flex-column gap-3">
                {filteredPeriods.map((p) => (
                    <div key={p.id} className="card shadow-sm">
                        <div className="card-body">
                            <div className="row align-items-center">
                                <div className="col-md-3">
                                    <small className="text-muted d-block">
                                        Období
                                    </small>
                                    {p.isCurrent && (
                                        <span className="badge bg-dark mb-1">
                                            Aktuálně neaktivní
                                        </span>
                                    )}
                                    <div className="text-muted small">
                                        ID období: {p.id}
                                    </div>
                                </div>

                                <div className="col-md-3">
                                    <small className="text-muted d-block">
                                        Neaktivní od
                                    </small>
                                    {formatDateTime(p.inactiveFrom)}
                                </div>

                                <div className="col-md-3">
                                    <small className="text-muted d-block">
                                        Neaktivní do
                                    </small>
                                    {formatDateTime(p.inactiveTo)}
                                </div>

                                <div className="col-md-3">
                                    <small className="text-muted d-block">
                                        Důvod neaktivity
                                    </small>
                                    {p.inactivityReason || "-"}
                                </div>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default MyInactivityPage;
