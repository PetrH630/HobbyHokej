// src/components/PastMatches.jsx
import { useMyPassedMatches } from "../../hooks/useMyPassedMatches";
import { useCurrentPlayer } from "../../hooks/useCurrentPlayer";
import MatchCard from "./MatchCard";
import { useNavigate } from "react-router-dom";

import "./PastMatches.css";

const PastMatches = () => {
    const { matches, loading, error } = useMyPassedMatches();
    const { currentPlayer } = useCurrentPlayer();
    const navigate = useNavigate();

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
                    <br />
                    Prosím vyberte si hráče, pro kterého chcete zobrazit
                    uplynulé zápasy.
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
            <div className="container mt-3">
                <h3 className="mb-3 text-center">
                    Uplynulé zápasy pro {currentPlayer.fullName}
                </h3>
                <p className="text-center">
                    Zatím nemáte žádné uplynulé zápasy.
                </p>
            </div>
        );
    }

    return (
        <div className="container mt-3">
            <h4 className="mb-3 text-center">
                Uplynulé zápasy:
            </h4>

            <div className="past-match-list">
                {[...matches]
                    .sort((a, b) => new Date(b.dateTime) - new Date(a.dateTime))
                    .map((m) => (
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
