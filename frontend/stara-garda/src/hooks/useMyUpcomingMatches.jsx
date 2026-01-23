// src/hooks/useMyUpcomingMatches.js
import { useEffect, useState } from "react";
import { getMyUpcomingMatchesOverview } from "../api/matchApi";

export const useMyUpcomingMatches = () => {
    const [matches, setMatches] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        let isMounted = true;

        const load = async () => {
            try {
                const data = await getMyUpcomingMatchesOverview();
                if (isMounted) {
                    setMatches(data);
                }
            } catch (err) {
                if (!isMounted) return;

                const status = err?.response?.status;
                const msg = err?.response?.data?.message;

                // typicky když není nastaven currentPlayer → 400/404
                if (status === 400 || status === 404) {
                    setError(
                        msg ||
                        "Nejprve si musíte vybrat aktuálního hráče."
                    );
                } else {
                    setError(
                        msg || "Nepodařilo se načíst nadcházející zápasy."
                    );
                }
            } finally {
                if (isMounted) {
                    setLoading(false);
                }
            }
        };

        load();

        return () => {
            isMounted = false;
        };
    }, []);

    return { matches, loading, error };
};
