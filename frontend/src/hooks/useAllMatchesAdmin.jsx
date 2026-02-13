// src/hooks/useAllMatchesAdmin.jsx
import { useEffect, useState, useCallback } from "react";
import { getAllMatchesAdmin } from "../api/matchApi";

export const useAllMatchesAdmin = (seasonId) => {
    const [matches, setMatches] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const load = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            // pokud backend bere sezónu z user settings,
            // seasonId klidně nepotřebuješ v parametrech,
            // ale použijeme ho aspoň v závislostech:
            const data = await getAllMatchesAdmin();
            // kdybys měl endpoint podle sezóny:
            // const data = await getAllMatchesAdmin(seasonId);
            setMatches(data);
        } catch (err) {
            console.error("Chyba při načítání zápasů:", err);
            const status = err?.response?.status;
            const msg = err?.response?.data?.message;

            if (status === 404) {
                setError(msg || "Žádné zápasy nebyly nalezeny.");
            } else {
                setError("Nepodařilo se načíst zápasy.");
            }
        } finally {
            setLoading(false);
        }
    }, [seasonId]); // 👈 DŮLEŽITÉ: závislost na seasonId

    useEffect(() => {
        // když seasonId ještě není známa (např. při prvním mountu), můžeš i tak načíst,
        // pokud backend bere aktuální sezónu z user settings:
        if (seasonId == null) {
            // buď vrať nic, nebo klidně zavolej load();
            // return;
        }
        load();
    }, [load]);

    return { matches, loading, error, reload: load };
};
