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
        </div>
    );
};

export default Home;
