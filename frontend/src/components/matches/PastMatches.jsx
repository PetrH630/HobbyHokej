import { useEffect, useMemo, useState } from "react";
import { useMyPassedMatches } from "../../hooks/useMyPassedMatches";
import { useCurrentPlayer } from "../../hooks/useCurrentPlayer";
import MatchCard from "./MatchCard";
import { useNavigate, useLocation } from "react-router-dom";
import "./PastMatches.css";

const FILTERS = {
    ALL: "ALL",
    REGISTERED: "REGISTERED",
    EXCUSED_UNREGISTERED: "EXCUSED_UNREGISTERED",
    NO_RESPONSE_SUBSTITUTE: "NO_RESPONSE_SUBSTITUTE",
};

const STORAGE_KEYS = {
    scrollY: "pastMatches.scrollY",
    filter: "pastMatches.filter",
    lastMatchId: "pastMatches.lastMatchId",
};

const MATCHES_KEYS = {
    disableAutoScrollOnce: "matches.disableAutoScrollOnce",
};

/**
 * Vrátí true, pokud daný zápas odpovídá zvolenému filtru.
 */
const matchPassesFilter = (match, filter) => {
    const status = match.playerMatchStatus;

    switch (filter) {
        case FILTERS.REGISTERED:
            return status === "REGISTERED";
        case FILTERS.EXCUSED_UNREGISTERED:
            return status === "EXCUSED" || status === "UNREGISTERED";
        case FILTERS.NO_RESPONSE_SUBSTITUTE:
            return status === "NO_RESPONSE" || status === "SUBSTITUTE";
        case FILTERS.ALL:
        default:
            return true;
    }
};

const PastMatches = () => {
    const { matches, loading, error } = useMyPassedMatches();
    const { currentPlayer } = useCurrentPlayer();
    const navigate = useNavigate();
    const location = useLocation();

    const [highlightedId, setHighlightedId] = useState(null);

    
    const [filter, setFilter] = useState(() => {
        const saved = sessionStorage.getItem(STORAGE_KEYS.filter);
        return saved && Object.values(FILTERS).includes(saved) ? saved : FILTERS.ALL;
    });

    const sortedMatches = useMemo(() => {
        return (matches || []).slice().sort((a, b) => new Date(b.dateTime) - new Date(a.dateTime));
    }, [matches]);

    const counts = useMemo(() => {
        return {
            all: sortedMatches.length,
            registered: sortedMatches.filter((m) => matchPassesFilter(m, FILTERS.REGISTERED)).length,
            excusedUnregistered: sortedMatches.filter((m) => matchPassesFilter(m, FILTERS.EXCUSED_UNREGISTERED)).length,
            noResponseSubstitute: sortedMatches.filter((m) => matchPassesFilter(m, FILTERS.NO_RESPONSE_SUBSTITUTE)).length,
        };
    }, [sortedMatches]);

    const filteredMatches = useMemo(() => {
        return sortedMatches.filter((m) => matchPassesFilter(m, filter));
    }, [sortedMatches, filter]);

    
    useEffect(() => {
        sessionStorage.setItem(STORAGE_KEYS.filter, filter);
    }, [filter]);

    
    useEffect(() => {
        if (loading) return;
        if (error) return;

        const savedY = sessionStorage.getItem(STORAGE_KEYS.scrollY);
        const lastId = sessionStorage.getItem(STORAGE_KEYS.lastMatchId);

        if (savedY != null) {
            const y = Number(savedY);

            requestAnimationFrame(() => {
                requestAnimationFrame(() => {
                    window.scrollTo({
                        top: Number.isFinite(y) ? y : 0,
                        behavior: "auto",
                    });
                });
            });

            sessionStorage.removeItem(STORAGE_KEYS.scrollY);
        }

        if (lastId != null) {
            const idNum = Number(lastId);
            if (Number.isFinite(idNum)) {
                setHighlightedId(idNum);
            }
        }
    }, [location.key, loading, error, filteredMatches.length]);

    
    useEffect(() => {
        if (highlightedId == null) return;

        const clear = () => {
            setHighlightedId(null);
            sessionStorage.removeItem(STORAGE_KEYS.lastMatchId);
            window.removeEventListener("pointerdown", clear, true);
        };

        window.addEventListener("pointerdown", clear, true);

        const t = window.setTimeout(() => clear(), 6000);

        return () => {
            window.clearTimeout(t);
            window.removeEventListener("pointerdown", clear, true);
        };
    }, [highlightedId]);

    const getFilterLabel = (f) => {
        switch (f) {
            case FILTERS.REGISTERED:
                return "Byl";
            case FILTERS.EXCUSED_UNREGISTERED:
                return "Odhlášen / omluven";
            case FILTERS.NO_RESPONSE_SUBSTITUTE:
                return "Nereagoval / možná";
            case FILTERS.ALL:
            default:
                return "Vše";
        }
    };

    const getFilterCount = (f) => {
        switch (f) {
            case FILTERS.REGISTERED:
                return counts.registered;
            case FILTERS.EXCUSED_UNREGISTERED:
                return counts.excusedUnregistered;
            case FILTERS.NO_RESPONSE_SUBSTITUTE:
                return counts.noResponseSubstitute;
            case FILTERS.ALL:
            default:
                return counts.all;
        }
    };

    if (loading) return <p>Načítám uplynulé zápasy…</p>;

    if (error) {
        return (
            <div className="container mt-4 text-center">
                <p className="mb-3 text-danger">{error}</p>
                <button className="btn btn-primary" onClick={() => navigate("/app/players")}>
                    Vybrat aktuálního hráče
                </button>
            </div>
        );
    }

    if (!currentPlayer) {
        return (
            <div className="container mt-4 text-center">
                <p className="mb-3">Nemáte vybraného aktuálního hráče.</p>
                <button className="btn btn-primary" onClick={() => navigate("/app/players")}>
                    Vybrat hráče
                </button>
            </div>
        );
    }

    if (sortedMatches.length === 0) {
        return (
            <div className="container mt-3 text-center">
                <h4>Uplynulé zápasy</h4>
                <p>Zatím nemáte žádné uplynulé zápasy.</p>
            </div>
        );
    }

    return (
        <div className="container mt-3">
            <h4 className="mb-3 text-center">Uplynulé zápasy:</h4>

            {/* ===== FILTR ===== */}
            <div className="mb-3">
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
                                <button className="dropdown-item" onClick={() => setFilter(FILTERS.ALL)}>
                                    Vše ({counts.all})
                                </button>
                            </li>
                            <li>
                                <button className="dropdown-item" onClick={() => setFilter(FILTERS.REGISTERED)}>
                                    Byl ({counts.registered})
                                </button>
                            </li>
                            <li>
                                <button className="dropdown-item" onClick={() => setFilter(FILTERS.EXCUSED_UNREGISTERED)}>
                                    Odhlášen / omluven ({counts.excusedUnregistered})
                                </button>
                            </li>
                            <li>
                                <button className="dropdown-item" onClick={() => setFilter(FILTERS.NO_RESPONSE_SUBSTITUTE)}>
                                    Nereagoval / možná ({counts.noResponseSubstitute})
                                </button>
                            </li>
                        </ul>
                    </div>
                </div>

                {/* 💻 DESKTOP – Button group */}
                <div className="d-none d-sm-flex justify-content-center">
                    <div className="btn-group" role="group" aria-label="Filtr uplynulých zápasů">
                        <button
                            type="button"
                            className={filter === FILTERS.ALL ? "btn btn-primary" : "btn btn-outline-primary"}
                            onClick={() => setFilter(FILTERS.ALL)}
                        >
                            Vše{" "}
                            <span className="badge bg-light text-dark ms-1">{counts.all}</span>
                        </button>

                        <button
                            type="button"
                            className={filter === FILTERS.REGISTERED ? "btn btn-primary" : "btn btn-outline-primary"}
                            onClick={() => setFilter(FILTERS.REGISTERED)}
                        >
                            Byl{" "}
                            <span className="badge bg-light text-dark ms-1">{counts.registered}</span>
                        </button>

                        <button
                            type="button"
                            className={filter === FILTERS.EXCUSED_UNREGISTERED ? "btn btn-primary" : "btn btn-outline-primary"}
                            onClick={() => setFilter(FILTERS.EXCUSED_UNREGISTERED)}
                        >
                            Odhlášen / omluven{" "}
                            <span className="badge bg-light text-dark ms-1">{counts.excusedUnregistered}</span>
                        </button>

                        <button
                            type="button"
                            className={filter === FILTERS.NO_RESPONSE_SUBSTITUTE ? "btn btn-primary" : "btn btn-outline-primary"}
                            onClick={() => setFilter(FILTERS.NO_RESPONSE_SUBSTITUTE)}
                        >
                            Nereagoval / možná{" "}
                            <span className="badge bg-light text-dark ms-1">{counts.noResponseSubstitute}</span>
                        </button>
                    </div>
                </div>
            </div>

            {filteredMatches.length === 0 && (
                <p className="text-center mb-3">Pro zvolený filtr nemáte žádné uplynulé zápasy.</p>
            )}

            <div className="past-match-list">
                {filteredMatches.map((m) => (
                    <div
                        className={`past-match-item ${highlightedId === m.id ? "past-match-item--highlight" : ""}`}
                        key={m.id}
                    >
                        <MatchCard
                            match={m}
                            onClick={() => {
                              
                                sessionStorage.setItem(MATCHES_KEYS.disableAutoScrollOnce, "1");
                              
                                sessionStorage.setItem(STORAGE_KEYS.scrollY, String(window.scrollY));
                                sessionStorage.setItem(STORAGE_KEYS.filter, filter);
                                sessionStorage.setItem(STORAGE_KEYS.lastMatchId, String(m.id));


                                if (document.activeElement && typeof document.activeElement.blur === "function") {
                                    document.activeElement.blur();
                                }

                                navigate(`/app/matches/${m.id}`, {
                                    state: { isPast: true },
                                });
                            }}
                            disabledTooltip="Nebyl jsi, nemáš oprávnění"
                            condensed
                        />
                    </div>
                ))}
            </div>
        </div>
    );
};

export default PastMatches;
