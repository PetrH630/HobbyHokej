// src/hooks/usePlayerRegistationHistoryAdmin.jsx

import { useEffect, useState } from "react";
import { getPlayerRegistrationHistoryAdmin } from "../api/matchRegistrationHistoryApi";

export const usePlayerRegistrationHistoryAdmin = (matchId, playerId) => {
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!matchId || !playerId) {
            setHistory([]);
            setLoading(false);
            setError("Chybí matchId nebo playerId.");
            return;
        }

        let isMounted = true;

        const load = async () => {
            try {
                setLoading(true);
                setError(null);
                const data = await getPlayerRegistrationHistoryAdmin(
                    matchId,
                    playerId
                );
                if (!isMounted) return;
                setHistory(data || []);
            } catch (err) {
                if (!isMounted) return;
                setError(
                    err?.response?.data?.message ||
                    "Nepodařilo se načíst historii registrací."
                );
            } finally {
                if (isMounted) setLoading(false);
            }
        };

        load();

        return () => {
            isMounted = false;
        };
    }, [matchId, playerId]);

    return { history, loading, error };
};
