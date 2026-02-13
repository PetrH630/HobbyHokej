// src/pages/Home.jsx
import MatchCard from "../components/matches/MatchCard";
import Players from "../components/players/Players";
import { useMyUpcomingMatches } from "../hooks/useMyUpcomingMatches";
import { useAuth } from "../hooks/useAuth";

const Home = () => {
    const { matches, loading, error } = useMyUpcomingMatches();
    const { user } = useAuth();

    if (loading) return <p>Načítám data…</p>;

    if (error) {
        return (
            <div className="container mt-3">
                <p className="text-danger">{error}</p>
            </div>
        );
    }

    return (
        <div className="container mt-3">
            <Players />

            <h3 className="mt-4 mb-3">Nadcházející zápasy</h3>

            {matches.length === 0 && (
                <p>Aktuálně nemáte žádné nadcházející zápasy.</p>
            )}

            <div className="match-list">
                {matches.map((match) => (
                    <div className="match-item" key={match.id}>
                        <MatchCard match={match} />
                    </div>
                ))}
            </div>
        </div>
    );
};

export default Home;
