// src/hooks/usePlayerInactivityPeriodsAdmin.js
import { useEffect, useState, useCallback } from "react";
import { getAllPlayerInactivityPeriodsAdmin } from "../api/playerInactivityApi";

export const usePlayerInactivityPeriodsAdmin = () => {
    const [periods, setPeriods] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchData = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);

            const data = await getAllPlayerInactivityPeriodsAdmin();
            setPeriods(data || []);
        } catch (err) {
            console.error("Chyba při načítání informací o neaktivitě hráčů:", err);
            setError(
                err?.response?.data?.message ||
                "Nepodařilo se načíst informace o neaktivitě hráčů."
            );
        } finally {
            setLoading(false);
        }
    }, []);

    // načtení při prvním renderu
    useEffect(() => {
        fetchData();
    }, [fetchData]);

    // funkce pro ruční reload (po uložení v modalu)
    const reload = useCallback(() => {
        fetchData();
    }, [fetchData]);

    return { periods, loading, error, reload };
};
