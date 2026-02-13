// src/hooks/useMyUpcomingMatches.js
import { useEffect, useState } from "react";
import { getMyUpcomingMatchesOverview } from "../api/matchApi";
import { useSeason } from "./useSeason";
import { useCurrentPlayer } from "./useCurrentPlayer";

export const useMyUpcomingMatches = () => {
    const { currentSeasonId } = useSeason();
    const { currentPlayer } = useCurrentPlayer(); // 👈 přidáno

    const [matches, setMatches] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        let isMounted = true;

        const load = async () => {
            // 1) Bez vybrané sezóny nemá smysl nic tahat
            if (!currentSeasonId) {
                if (isMounted) {
                    setMatches([]);
                    setLoading(false);
                    setError(null);
                }
                return;
            }

            // 2) Bez aktuálního hráče také ne – počkáme, než si ho uživatel vybere
            if (!currentPlayer) {
                if (isMounted) {
                    setMatches([]);
                    setLoading(false);
                    setError("Nejprve si musíte vybrat aktuálního hráče.");
                }
                return;
            }

            if (isMounted) {
                setLoading(true);
                setError(null);
            }

            try {
                const data = await getMyUpcomingMatchesOverview();
                if (isMounted) {
                    setMatches(data);
                }
            } catch (err) {
                if (!isMounted) return;

                const status = err?.response?.status;
                const msg = err?.response?.data?.message;

                if (status === 400 || status === 404) {
                    setError(
                        msg ||
                        "Nejprve si musíte vybrat aktuálního hráče."
                    );
                } else {
                    setError(
                        msg ||
                        "Nepodařilo se načíst nadcházející zápasy."
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
        // 👇 reaguje na změnu sezóny i aktuálního hráče
    }, [currentSeasonId, currentPlayer?.id]);

    return { matches, loading, error };
};
