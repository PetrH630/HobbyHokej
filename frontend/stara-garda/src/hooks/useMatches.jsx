//custom hook pro načtení zápasů
//Výhoda: UI komponenta jen vypisuje, hook řeší data fetching.

import { useState, useEffect } from "react";
import api from "../api/axios";

export const useMatches = () => {
    const [matches, setMatches] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        api.get("/matches/me/upcoming-overview")
            .then(res => setMatches(res.data))
            .catch(console.error)
            .finally(() => setLoading(false));
    }, []);

    return { matches, loading };
};
