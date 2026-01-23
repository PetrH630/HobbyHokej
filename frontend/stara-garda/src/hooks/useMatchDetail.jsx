// src/hooks/useMatchDetail.js
import { useEffect, useState, useCallback } from "react";
import { getMatchDetail } from "../api/matchApi";

export const useMatchDetail = (matchId) => {
    const [match, setMatch] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const reload = useCallback(async () => {
        if (!matchId) return;
        try {
            setLoading(true);
            setError(null);
            const data = await getMatchDetail(matchId);
            setMatch(data);
        } catch (err) {
            console.error(err);
            setError(
                err?.response?.data?.message || "Nepodařilo se načíst detail zápasu."
            );
        } finally {
            setLoading(false);
        }
    }, [matchId]);

    useEffect(() => {
        reload();
    }, [reload]);

    return { match, loading, error, reload };
};
