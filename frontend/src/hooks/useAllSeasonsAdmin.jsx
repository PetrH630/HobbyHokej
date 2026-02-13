// src/hooks/useAllSeasonsAdmin.js
import { useEffect, useState } from "react";
import { getAllSeasonsAdmin } from "../api/seasonApi";

/**
 * Hook pro globální správu sezón (ADMIN / MANAGER).
 *
 * Načítá všechny sezóny v systému přes GET /api/seasons.
 * Vrací:
 *  - seasons: pole všech sezón (SeasonDTO[])
 *  - loading: true, dokud se načítá
 *  - error: text chyby nebo null
 *  - reload(): znovu načte data (po create/update/delete)
 */
export const useAllSeasonsAdmin = () => {
    const [seasons, setSeasons] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const loadSeasons = async () => {
        setLoading(true);
        setError(null);

        try {
            const data = await getAllSeasonsAdmin();
            setSeasons(data);
            return data;
        } catch (err) {
            console.error("Nepodařilo se načíst sezóny", err);

            const message =
                err?.response?.data?.message ||
                err?.message ||
                "Nepodařilo se načíst sezóny";

            setError(message);
            return null;
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadSeasons();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return {
        seasons,
        loading,
        error,
        reload: loadSeasons,
    };
};
