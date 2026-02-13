// src/hooks/usePlayerHistoryAdmin.jsx
import { useEffect, useState } from "react";
import { getPlayerHistoryAdmin } from "../api/playerApi";

export const usePlayerHistoryAdmin = (playerId) => {
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!playerId) {
            setHistory([]);
            setLoading(false);
            setError(null);
            return;
        }

        let isMounted = true;

        const load = async () => {
            setLoading(true);
            setError(null);

            try {
                const data = await getPlayerHistoryAdmin(playerId);
                if (!isMounted) return;
                setHistory(data || []);
            } catch (err) {
                if (!isMounted) return;
                console.error("Chyba při načítání historie hráče:", err);
                const msg =
                    err?.response?.data?.message ||
                    err?.message ||
                    "Nepodařilo se načíst historii hráče.";
                setError(msg);
            } finally {
                if (isMounted) setLoading(false);
            }
        };

        load();

        return () => {
            isMounted = false;
        };
    }, [playerId]);

    return { history, loading, error };
};
