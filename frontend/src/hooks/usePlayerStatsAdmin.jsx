// src/hooks/usePlayerStatsAdmin.js
import { useCallback, useEffect, useState } from "react";
import { getPlayerStatsAdmin } from "../api/playerApi";

/**
 * Hook pro načtení statistik vybraného hráče (ADMIN/MANAGER).
 *
 * Data se načítají z backendu přes GET /api/players/{playerId}/stats.
 */
export const usePlayerStatsAdmin = (playerId, options = {}) => {
    const { enabled = true } = options;

    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(Boolean(enabled));
    const [error, setError] = useState("");

    const reload = useCallback(async () => {
        if (!enabled) return;
        if (!playerId) return;

        setLoading(true);
        setError("");

        try {
            const data = await getPlayerStatsAdmin(playerId);
            setStats(data ?? null);
        } catch (e) {
            const message =
                e?.response?.data?.message ||
                e?.message ||
                "Nepodařilo se načíst statistiky hráče.";
            setError(message);
            setStats(null);
        } finally {
            setLoading(false);
        }
    }, [enabled, playerId]);

    useEffect(() => {
        reload();
    }, [reload]);

    return { stats, loading, error, reload };
};
