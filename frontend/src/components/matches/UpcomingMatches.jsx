// src/components/matches/UpcomingMatches.jsx
import { useMyUpcomingMatches } from "../../hooks/useMyUpcomingMatches";
import { useCurrentPlayer } from "../../hooks/useCurrentPlayer";
import MatchCard from "./MatchCard";
import { useNavigate } from "react-router-dom";

import "./UpcomingMatches.css";

const UpcomingMatches = () => {
  const { matches, loading, error } = useMyUpcomingMatches();
  const { currentPlayer } = useCurrentPlayer();
  const navigate = useNavigate();

  if (loading) {
    return <p>Načítám nadcházející zápasy…</p>;
  }

  if (error) {
    return (
      <div className="container mt-4 text-center">
        <p className="mb-3 text-danger">{error}</p>
        <button
          className="btn btn-primary"
          onClick={() => navigate("/app/players")}
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
          nadcházející zápasy.
        </p>
        <button
          className="btn btn-primary"
          onClick={() => navigate("/app/players")}
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
          Nadcházející zápasy pro {currentPlayer.fullName}
        </h3>
        <div className="text-center">
          <p>Aktuálně nemáte žádné nadcházející zápasy.</p>
          <p>Možná nejsou v této sezóně zatím žádné vytvořené, </p>
          <p>nebo jste veden jako dlouhodobě "Mimo Hru".</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mt-3">
      <h4 className="mb-1 text-center player-name">
        {currentPlayer.fullName}
      </h4>
      <h4 className="mb-3 text-center">
        Nadcházející zápasy:
      </h4>

      <div className="match-list">
        {matches.map((m) => (
          <div className="match-item" key={m.id}>
            <MatchCard
              match={m}
              onClick={() =>
                navigate(`/app/matches/${m.id}`, {
                  state: { isPast: false },
                })
              }
            />
          </div>
        ))}
      </div>
    </div>
  );
};

export default UpcomingMatches;
