// src/components/matches/UpcomingMatches.jsx
import { useEffect, useRef } from "react";
import { useMyUpcomingMatches } from "../../hooks/useMyUpcomingMatches";
import { useCurrentPlayer } from "../../hooks/useCurrentPlayer";
import MatchCard from "./MatchCard";
import { useNavigate } from "react-router-dom";

import "./UpcomingMatches.css";

const UpcomingMatches = () => {
  const { matches, loading, error } = useMyUpcomingMatches();
  const { currentPlayer } = useCurrentPlayer();
  const navigate = useNavigate();

  const firstMatchRef = useRef(null);
  const hasAutoScrolledRef = useRef(false);

  useEffect(() => {
    if (loading || error) return;
    if (!currentPlayer) return;
    if (!Array.isArray(matches) || matches.length === 0) return;

    // jen jednou po načtení
    if (hasAutoScrolledRef.current) return;

    const isSmallDevice = window.matchMedia("(max-width: 767.98px)").matches;
    if (!isSmallDevice) return;

    // auto-scroll jen pokud je uživatel "nahoře" (nezlobíme, když už scrolluje)
    const isNearTop = window.scrollY < 200;
    if (!isNearTop) return;

    if (firstMatchRef.current) {
      hasAutoScrolledRef.current = true;

      window.setTimeout(() => {
        firstMatchRef.current?.scrollIntoView({
          behavior: "smooth",
          block: "start",
        });
      }, 150);
    }
  }, [loading, error, currentPlayer, matches]);

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
        {matches.map((m, idx) => {
          const isFirst = idx === 0;
          const isNextHighlighted = isFirst && m.matchStatus !== "CANCELED";

          return (
            <div
              className={`match-item ${isFirst ? "match-item--next" : ""}`}
              key={m.id}
              ref={isFirst ? firstMatchRef : null}
            >
              <div
                className={`next-match-frame ${isNextHighlighted ? "next-match-frame--on" : ""
                  }`}
                aria-label={isNextHighlighted ? "Nejbližší zápas" : undefined}
              >
                {isNextHighlighted && (
                  <div className="next-match-badge">
                    Nejbližší zápas
                  </div>
                )}

                <MatchCard
                  match={m}
                  onClick={() =>
                    navigate(`/app/matches/${m.id}`, {
                      state: { isPast: false },
                    })
                  }
                />
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default UpcomingMatches;