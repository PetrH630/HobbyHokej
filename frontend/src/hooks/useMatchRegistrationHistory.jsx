// src/hooks/useMatchRegistrationHistory.jsx
import { useEffect, useState } from "react";
import { getMyMatchRegistrationHistory } from "../api/matchRegistrationHistoryApi";


export const useMyMatchRegistrationHistory = (matchId) => {
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!matchId) {
            setHistory([]);
            setLoading(false);
            return;
        }

        const load = async () => {
            try {
                setLoading(true);
                const data = await getMyMatchRegistrationHistory(matchId);
                setHistory(data);
                setError(null);
            } catch (err) {
                setError(
                    err?.response?.data?.message ||
                    "Nepodařilo se načíst historii registrací."
                );
            } finally {
                setLoading(false);
            }
        };

        load();
    }, [matchId]);


    return { history, loading, error };
};
