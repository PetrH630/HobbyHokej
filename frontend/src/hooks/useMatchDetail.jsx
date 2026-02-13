// src/hooks/useMatchDetail.jsx
import { useEffect, useState, useCallback } from "react";
import { getMatchDetail } from "../api/matchApi"; // uprav název funkce podle tvého API

export const useMatchDetail = (id) => {
    const [match, setMatch] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const load = useCallback(async () => {
        if (!id) {
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const data = await getMatchDetail(id);
            console.log("Načtený detail zápasu:", data);
            setMatch(data);
        } catch (err) {
            console.error("Chyba při načítání detailu zápasu:", err);

            const status = err?.response?.status;
            const msg = err?.response?.data?.message;

            if (status === 404) {
                setError(msg || "Zápas nebyl nalezen.");
            } else if (status === 403) {
                setError("Nemáte oprávnění zobrazit tento zápas.");
            } else {
                setError("Nepodařilo se načíst detail zápasu.");
            }
        } finally {
            setLoading(false);
        }
    }, [id]);

    useEffect(() => {
        load();
    }, [load]);

    return { match, loading, error, reload: load };
};
