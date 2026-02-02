// src/components/matches/PastMatches.jsx
import { useState } from "react";
import { useMyPassedMatches } from "../../hooks/useMyPassedMatches";
import { useCurrentPlayer } from "../../hooks/useCurrentPlayer";
import MatchCard from "./MatchCard";
import { useNavigate } from "react-router-dom";

import "./PastMatches.css";

const FILTERS = {
    ALL: "ALL",
    REGISTERED: "REGISTERED",
    EXCUSED_UNREGISTERED: "EXCUSED_UNREGISTERED",
    NO_RESPONSE_SUBSTITUTE: "NO_RESPONSE_SUBSTITUTE",
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

    const [filter, setFilter] = useState(FILTERS.ALL);

    if (loading) {
        return <p>Načítám uplynulé zápasy…</p>;
    }

    if (error) {
        return (
            <div className="container mt-4 text-center">
                <p className="mb-3 text-danger">{error}</p>
                <button
                    className="btn btn-primary"
                    onClick={() => navigate("/players")}
                >
                    Vybrat aktuálního hráče
                </button>
            </div>
        );
    }

    if (!currentPlayer) {
        return (
            <div className="container mt-4 text-center">
                <p className="mb-3">
                    Nemáte vybraného aktuálního hráče.
                </p>
                <button
                    className="btn btn-primary"
                    onClick={() => navigate("/players")}
                >
                    Vybrat hráče
                </button>
            </div>
        );
    }

    if (matches.length === 0) {
        return (
            <div className="container mt-3 text-center">
                <h4>Uplynulé zápasy</h4>
                <p>Zatím nemáte žádné uplynulé zápasy.</p>
            </div>
        );
    }

    const sortedMatches = matches
        .slice()
        .sort((a, b) => new Date(b.dateTime) - new Date(a.dateTime));

    // počty pro badge u jednotlivých filtrů
    const counts = {
        all: sortedMatches.length,
        registered: sortedMatches.filter((m) =>
            matchPassesFilter(m, FILTERS.REGISTERED)
        ).length,
        excusedUnregistered: sortedMatches.filter((m) =>
            matchPassesFilter(m, FILTERS.EXCUSED_UNREGISTERED)
        ).length,
        noResponseSubstitute: sortedMatches.filter((m) =>
            matchPassesFilter(m, FILTERS.NO_RESPONSE_SUBSTITUTE)
        ).length,
    };

    const filteredMatches = sortedMatches.filter((m) =>
        matchPassesFilter(m, filter)
    );

    return (
        <div className="container mt-3">
            <h4 className="mb-3 text-center">
                Uplynulé zápasy pro {currentPlayer.fullName}
            </h4>

            {/* Filtrovací tlačítka */}
            <div className="d-flex justify-content-center mb-3">
                <div
                    className="btn-group"
                    role="group"
                    aria-label="Filtr uplynulých zápasů"
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
                            filter === FILTERS.REGISTERED
                                ? "btn btn-primary"
                                : "btn btn-outline-primary"
                        }
                        onClick={() => setFilter(FILTERS.REGISTERED)}
                    >
                        Byl{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.registered}
                        </span>
                    </button>

                    <button
                        type="button"
                        className={
                            filter === FILTERS.EXCUSED_UNREGISTERED
                                ? "btn btn-primary"
                                : "btn btn-outline-primary"
                        }
                        onClick={() => setFilter(FILTERS.EXCUSED_UNREGISTERED)}
                    >
                        Odhlášen / omluven{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.excusedUnregistered}
                        </span>
                    </button>

                    <button
                        type="button"
                        className={
                            filter === FILTERS.NO_RESPONSE_SUBSTITUTE
                                ? "btn btn-primary"
                                : "btn btn-outline-primary"
                        }
                        onClick={() =>
                            setFilter(FILTERS.NO_RESPONSE_SUBSTITUTE)
                        }
                    >
                        Nereagoval / možná{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.noResponseSubstitute}
                        </span>
                    </button>
                </div>
            </div>

            {/* Info, když filtr nic nevrátí */}
            {filteredMatches.length === 0 && (
                <p className="text-center mb-3">
                    Pro zvolený filtr nemáte žádné uplynulé zápasy.
                </p>
            )}

            <div className="past-match-list">
                {filteredMatches.map((m) => (
                    <div className="past-match-item" key={m.id}>
                        <MatchCard
                            match={m}
                            onClick={() =>
                                navigate(`/matches/${m.id}`, {
                                    state: { isPast: true },
                                })
                            }
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
