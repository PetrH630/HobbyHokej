import MatchCard from "../components/MatchCard";
import Player from "../components/Player";
import { useMatches } from "../hooks/useMatches";
import { useAuth } from "../hooks/useAuth";

const Home = () => {
    const { matches, loading } = useMatches();
    const { user } = useAuth();

    if (loading) return <p>Načítám data…</p>;

    return (
        <div>
           <Player />
            <h3>Nadcházející zápasy</h3>
            {matches.map(match => (
                <MatchCard key={match.id} match={match} showPricePerPlayer />
            ))}
        </div>
    );
};

export default Home;
