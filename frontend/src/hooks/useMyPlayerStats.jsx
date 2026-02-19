// src/hooks/useMyPlayerStats.js
import { useCallback, useEffect, useRef, useState } from "react";
import { getMyPlayerStats } from "../api/playerApi";
import { useCurrentPlayer } from "./useCurrentPlayer";
import { useSeason } from "./useSeason";

/**
 * Hook pro načtení statistik aktuálního hráče přihlášeného uživatele.
 *
 * Hook se používá na hráčských stránkách, kde je potřeba zobrazit agregované
 * statistiky registrací a účasti na zápasech pro aktuální sezónu.
 *
 * Data se načítají z backendu přes GET /api/players/me/stats.
 *
 * Statistiky se automaticky obnovují při změně:
 *  - aktuálního hráče (CurrentPlayerContext)
 *  - aktuální sezóny (SeasonContext)
 */
export const useMyPlayerStats = (options = {}) => {
    const { enabled = true } = options;

    const { currentPlayer } = useCurrentPlayer();
    const { currentSeasonId } = useSeason();

    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(Boolean(enabled));
    const [error, setError] = useState("");

    // ochrana proti přepsání novějších dat starším requestem
    const requestSeqRef = useRef(0);

    const reload = useCallback(async () => {
        if (!enabled) return;

        // bez vybraného hráče nedává smysl tahat stats (backend stejně vyhodí error)
        if (!currentPlayer?.id) {
            setStats(null);
            setLoading(false);
            setError("");
            return;
        }

        // pokud backend používá currentSeasonId z kontextu/session, necháme tady guard
        // (pokud není sezóna vybraná, statistiky zatím nenačítáme)
        if (!currentSeasonId) {
            setStats(null);
            setLoading(false);
            setError("");
            return;
        }

        const seq = ++requestSeqRef.current;

        setLoading(true);
        setError("");

        try {
            const data = await getMyPlayerStats();

            // ignoruj výsledek, pokud mezitím doběhl novější request
            if (seq !== requestSeqRef.current) return;

            setStats(data ?? null);
        } catch (e) {
            if (seq !== requestSeqRef.current) return;

            const message =
                e?.response?.data?.message ||
                e?.message ||
                "Nepodařilo se načíst statistiky hráče.";

            setError(message);
            setStats(null);
        } finally {
            if (seq !== requestSeqRef.current) return;
            setLoading(false);
        }
    }, [enabled, currentPlayer?.id, currentSeasonId]);

    useEffect(() => {
        reload();
    }, [reload]);

    return { stats, loading, error, reload };
};
