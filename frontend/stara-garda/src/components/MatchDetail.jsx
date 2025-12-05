import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

const MatchDetail = () => {
    const { matchId } = useParams();
    const [match, setMatch] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetch(`http://localhost:8080/api/matches/matchDetail/${matchId}`)
            .then(res => {
                if (!res.ok) throw new Error("Nepodařilo se načíst zápas");
                return res.json();
            })
            .then(data => setMatch(data))
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [matchId]);

    if (loading) return <p>Načítám zápas...</p>;
    if (error) return <p>Chyba: {error}</p>;
    if (!match) return <p>Žádný zápas nenalezen.</p>;

    return (
        <div className="match-detail">
            <h2>Zápas #{match.id}</h2>
            <p><strong>Datum a čas:</strong> {new Date(match.dateTime).toLocaleString()}</p>
            <p><strong>Maximální hráči:</strong> {match.maxPlayers}</p>
            <p><strong>Hráči v zápase:</strong> {match.inGamePlayers}</p>
            <p><strong>Hráči mimo zápas:</strong> {match.outGamePlayers}</p>
            <p><strong>Hráči čekající:</strong> {match.waitingPlayers}</p>
            <p><strong>Hráči bez akce:</strong> {match.noActionPlayers}</p>
            <p><strong>Volná místa:</strong> {match.remainingSlots}</p>
            <p><strong>Cena na hráče:</strong> {match.pricePerRegisteredPlayer} Kč</p>

            <h3>Zaregistrovaní hráči</h3>
            <ul>{match.registeredPlayers?.map(player => <li key={player}>{player}</li>)}</ul>

            <h3>Rezervovaní hráči</h3>
            <ul>{match.reservedPlayers?.map(player => <li key={player}>{player}</li>)}</ul>

            <h3>Nezaregistrovaní hráči</h3>
            <ul>{match.unregisteredPlayers?.map(player => <li key={player}>{player}</li>)}</ul>

            <h3>Omluveni hráči</h3>
            <ul>{match.excusedPlayers?.map(player => <li key={player}>{player}</li>)}</ul>

            <h3>Hráči bez odpovědi</h3>
            <ul>{match.noResponsePlayers?.map(player => <li key={player}>{player}</li>)}</ul>
        </div>
    );
};

export default MatchDetail;
