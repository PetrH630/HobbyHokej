// src/pages/matches.jsx
import React, { useEffect, useMemo } from "react";
import { useLocation } from "react-router-dom";
import UpcomingMatches from "../components/matches/UpcomingMatches";
import PastMatches from "../components/matches/PastMatches";
import { useCurrentPlayer } from "../hooks/useCurrentPlayer";
import SeasonSelect from "../components/seasons/seasonSelect";
import { useSeason } from "../hooks/useSeason";

const matches = () => {
  const { currentPlayer, loading } = useCurrentPlayer();
  const location = useLocation();

  const { seasons, currentSeasonId } = useSeason();

  useEffect(() => {
    const y = location.state?.restoreScrollY;
    if (typeof y === "number") {
      window.setTimeout(() => window.scrollTo(0, y), 0);
    }
  }, [location.state]);

  // helper na parsování datumu (stejný princip jako u zápasů)
  const parseDate = (raw) => {
    if (!raw) return null;
    const safe = raw.includes("T") ? raw : raw.replace(" ", "T");
    const d = new Date(safe);
    return Number.isNaN(d.getTime()) ? null : d;
  };

  const currentSeason = useMemo(
    () => seasons.find((s) => s.id === currentSeasonId) || null,
    [seasons, currentSeasonId]
  );

  const isSeasonFinished = useMemo(() => {
    if (!currentSeason) return false;

    // snaž se chytit typické názvy fieldů
    const endRaw =
      currentSeason.endDate ||
      currentSeason.end ||
      currentSeason.endAt ||
      currentSeason.endDateTime;

    if (!endRaw) return false;

    const end = parseDate(endRaw);
    if (!end) return false;

    const now = new Date();
    return end.getTime() < now.getTime();
  }, [currentSeason]);

  if (loading) {
    return <p>Načítám…</p>;
  }

  return (
    <div>
      {/* horní řádek – vlevo výběr sezóny */}
      <div className="d-flex justify-content-start mb-3">
        <SeasonSelect />
      </div>

      {isSeasonFinished ? (
        <div className="alert alert-info">
          Tato sezóna již skončila, zobrazuje se jen historie zápasů.
        </div>
      ) : (
        <UpcomingMatches />
      )}

      <hr className="my-4" />

      {currentPlayer && <PastMatches />}
    </div>
  );
};

export default matches;