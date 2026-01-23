// src/components/Match.jsx
import { useMyUpcomingMatches } from "../hooks/useMyUpcomingMatches";
import { useCurrentPlayer } from "../hooks/useCurrentPlayer";
import MatchCard from "./MatchCard";
import { useNavigate } from "react-router-dom";

import "./UpcomingMatches.css";

const UpcomingMatches = () => {
  const { matches, loading, error } = useMyUpcomingMatches();
  const { currentPlayer } = useCurrentPlayer();
  const navigate = useNavigate();

  // načítání
  if (loading) {
    return <p>Načítám nadcházející zápasy…</p>;
  }

  // chyba z hooku (typicky: není currentPlayer nebo problém na backendu)
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

  // pro jistotu – kdyby hook prošel bez erroru, ale currentPlayer chyběl
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
          onClick={() => navigate("/players")}
        >
          Vybrat hráče
        </button>
      </div>
    );
  }

  // žádné nadcházející zápasy
  if (matches.length === 0) {
    return (
      <div className="container mt-3">
        <h3 className="mb-3 text-center">
          Nadcházející zápasy pro {currentPlayer.fullName}
        </h3>
        <p className="text-center">
          Aktuálně nemáte žádné nadcházející zápasy.
        </p>
      </div>
    );
  }

  // standardní zobrazení seznamu zápasů
  return (
    <div className="container mt-3">
      <h4 className="mb-3 text-center player-name">
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
              // klik → detail zápasu (nebo cokoliv chceš)
              onClick={() => navigate(`/matches/${m.id}`, {
                state:{ status: m.status, isPast: false },
              })}              
            />
          </div>
        ))}
      </div>
    </div>
  );
};

export default UpcomingMatches;
