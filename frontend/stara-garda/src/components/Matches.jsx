import React, { useEffect, useState } from 'react';
import { getMatches } from '../services/api';
import MatchCard from './MatchCard';

const Matches = () => {
    const [matches, setMatches] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        getMatches()
            .then(data => {
                setMatches(data);
                setLoading(false);
            })
            .catch(err => {
                setError("Nepodařilo se načíst zápasy");
                setLoading(false);
            });
    }, []);

    if (loading) return <p>Načítám zápasy...</p>;
    if (error) return <p>Chyba: {error}</p>;

    return (
        <div className="container mt-4">
            <h2 className="mb-4">Všechny zápasy</h2>
            <div className="row">
                {matches.map(match => (
                    <div key={match.id} className="col-md-4 mb-3">
                        <MatchCard match={match} />
                    </div>
                ))}
            </div>
        </div>
    );
};

export default Matches;
