import React, { useEffect, useState } from "react";

import axios from "axios";
import MatchOverview from "../components/MatchOverview"

const Home = () => {
    const [matches, setMatches] = useState([]);
    const [loading, setLoading] = useState(true);
    

    useEffect(() => {
        // Načítáme zápasy pro přihlášeného uživatele
        axios.get("http://localhost:8080/api/matches/me/upcoming-overview", { withCredentials: true })
            .then(res => {
                console.log("MATCHES DATA:", res.data);
                setMatches(res.data);
            })
            .catch(err => {
                console.error("CHYBA při načítání zápasů:", err);
            })
            .finally(() => setLoading(false));
    }, []);

    if (loading) return <p>Načítám zápasy…</p>;

    return (
        <div className="container mt-4">
            <h1>Vítejte ve Stará Garda</h1>

            {matches.length === 0 && <p>Žádné nadcházející zápasy nejsou k dispozici</p>}

            {matches.map(match => (
                <MatchOverview key={match.id} match={match} />
            ))}
        </div>
    );
};

export default Home;
