import React from 'react'
import UpcomingMatches from "../components/matches/UpcomingMatches";
import PastMatches from "../components/matches/PastMatches";
import { useCurrentPlayer } from "../hooks/useCurrentPlayer";
import BackButton from "../components/BackButton";

const matches = () => {
  const { currentPlayer, loading } = useCurrentPlayer();

  if (loading) {
    return <p>Načítám…</p>;
  }

  return (
    <div>
      
      <UpcomingMatches />
      <BackButton />
      <hr className="my-4" />

      {currentPlayer && (
        <PastMatches />
      )}
      <BackButton />
      
    </div>
  )
}

export default matches