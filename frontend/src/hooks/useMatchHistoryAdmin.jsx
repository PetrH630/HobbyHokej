// src/hooks/useMatchHistoryAdmin.js
import { useEffect, useState } from "react";
import { getMatchHistoryAdmin } from "../api/matchApi";

export const useMatchHistoryAdmin = (matchId) => {
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!matchId) {
            setHistory([]);
            setLoading(false);
            setError("Chybí ID zápasu.");
            return;
        }

        let isMounted = true;

        const load = async () => {
            setLoading(true);
            setError(null);
            try {
                const data = await getMatchHistoryAdmin(matchId);
                if (!isMounted) return;
                setHistory(data || []);
            } catch (err) {
                if (!isMounted) return;

                const status = err?.response?.status;
                const msg = err?.response?.data?.message;

                if (status === 404) {
                    setError(msg || "Historie zápasu nebyla nalezena.");
                } else {
                    setError("Nepodařilo se načíst historii zápasu.");
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
    }, [matchId]);

    return { history, loading, error };
};
