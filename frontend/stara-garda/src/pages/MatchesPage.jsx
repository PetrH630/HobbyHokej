import React from 'react'
import UpcomingMatches from "../components/UpcomingMatches";
import PastMatches from "../components/PastMatches";
import { useCurrentPlayer } from "../hooks/useCurrentPlayer";

const matches = () => {
  const { currentPlayer, loading } = useCurrentPlayer();

  if (loading) {
    return <p>Načítám…</p>;
  }

  return (
    <div>
      
      <UpcomingMatches />
      <hr className="my-4" />

      {currentPlayer && (
        <PastMatches />
      )}

      
    </div>
  )
}

export default matches