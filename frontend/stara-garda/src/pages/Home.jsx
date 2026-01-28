import MatchCard from "../components/matches/MatchCard";
import Players from "../components/players/Players";
import { useMyUpcomingMatches } from "../hooks/useMyUpcomingMatches";
import { useAuth } from "../hooks/useAuth";

const Home = () => {
    const { matches, loading } = useUpcomingMatches();
    const { user } = useAuth();

    if (loading) return <p>Načítám data…</p>;

    return (
        <div>
            <Players />
            <h3>Nadcházející zápasy</h3>
            {matches.map(match => (
                <MatchCard key={match.id} match={match} showPricePerPlayer />
            ))}
        </div>
    );
};

export default Home;
