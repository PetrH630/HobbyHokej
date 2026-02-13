// src/hooks/useAllPlayersAdmin.js
import { useEffect, useState } from "react";
import { getAllPlayersAdmin } from "../api/playerApi";

/**
 * Hook pro globální správu hráčů (ADMIN / MANAGER).
 *
 * Načítá všechny hráče v systému přes GET /api/players.
 * Vrací:
 *  - players: pole všech hráčů (PlayerDTO[])
 *  - loading: true, dokud se načítá
 *  - error: text chyby nebo null
 *  - reload(): znovu načte data (po approve/reject/update/delete)
 */
export const useAllPlayersAdmin = () => {
    const [players, setPlayers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const loadPlayers = async () => {
        setLoading(true);
        setError(null);

        try {
            const data = await getAllPlayersAdmin();
            setPlayers(data);
            return data;
        } catch (err) {
            console.error("Nepodařilo se načíst všechny hráče", err);

            const message =
                err?.response?.data?.message ||
                err?.message ||
                "Nepodařilo se načíst hráče";

            setError(message);
            return null;
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        // načtení při prvním zobrazení admin stránky
        loadPlayers();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return {
        players,
        loading,
        error,
        reload: loadPlayers,
    };
};
