import React from 'react'
import UpcomingMatches from "../components/matches/UpcomingMatches";
import PastMatches from "../components/matches/PastMatches";
import { useCurrentPlayer } from "../hooks/useCurrentPlayer";
import BackButton from "../components/BackButton";
import SeasonSelect from "../components/seasons/seasonSelect";

const matches = () => {
  const { currentPlayer, loading } = useCurrentPlayer();

  if (loading) {
    return <p>Načítám…</p>;
  }

  return (
    <div>
      {/* horní řádek – vlevo výběr sezóny */}
      <div className="d-flex justify-content-start mb-3">
        <SeasonSelect />
      </div>
      <UpcomingMatches />
     
      <hr className="my-4" />

      {currentPlayer && (
        <PastMatches />
      )}
      

    </div>
  )
}

export default matches