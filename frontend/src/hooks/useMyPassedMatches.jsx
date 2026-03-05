import { useEffect, useState } from "react";
import { getMyPassedMatchesOverview } from "../api/matchApi";
import { useSeason } from "./useSeason";
import { useCurrentPlayer } from "./useCurrentPlayer";

/**
 * useMyPassedMatches
 *
 * Hook pro načtení minulých zápasů relevantních pro aktuálního hráče.
 * Používá se na stránkách historie a statistik.
 */
export const useMyPassedMatches = () => {
    const { currentSeasonId } = useSeason();
    const { currentPlayer } = useCurrentPlayer(); // 👈 přidáno

    const [matches, setMatches] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        let isMounted = true;

        const load = async () => {
            // 1) bez sezóny nemá smysl nic tahat
            if (!currentSeasonId) {
                if (isMounted) {
                    setMatches([]);
                    setLoading(false);
                    setError(null);
                }
                return;
            }

            // 2) bez aktuálního hráče taky ne – počkáme, až si ho uživatel vybere
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
                // backend bere aktuálního hráče i sezónu z kontextu uživatele
                const data = await getMyPassedMatchesOverview();
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
                        "Nepodařilo se načíst uplynulé zápasy."
                    );
                }
            } finally {
                if (isMounted) {
                    setLoading(false);
                }
            }
        };

        load();

        // úklid
        return () => {
            isMounted = false;
        };
        // 👇 reaguje na změnu sezóny i hráče
    }, [currentSeasonId, currentPlayer?.id]);

    return { matches, loading, error };
};
