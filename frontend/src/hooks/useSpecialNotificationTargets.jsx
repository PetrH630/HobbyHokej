// src/hooks/useSpecialNotificationTargets.js
import { useEffect, useState, useCallback } from "react";
import { fetchSpecialNotificationTargets } from "../api/notificationsApi";

/**
 * Hook pro načítání možných příjemců speciální notifikace.
 *
 * Hook volá endpoint /api/notifications/admin/special/targets
 * a vrací seznam cílů, stav načítání a případnou chybu.
 *
 * Vlastnosti:
 * - automatické načtení při prvním použití (autoLoad = true),
 * - možnost manuálního znovunačtení přes reload(),
 * - chyba je vrácena jako řetězec vhodný pro zobrazení v UI.
 *
 * @param {boolean} [autoLoad=true] Určuje, zda se mají cíle načíst automaticky při prvním renderu.
 * @returns {{
 *   targets: Array<Object>,
 *   loading: boolean,
 *   error: string | null,
 *   reload: () => Promise<void>
 * }}
 */
export const useSpecialNotificationTargets = (autoLoad = true) => {
    const [targets, setTargets] = useState([]);
    const [loading, setLoading] = useState(autoLoad);
    const [error, setError] = useState(null);

    const loadTargets = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const data = await fetchSpecialNotificationTargets();
            setTargets(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error("Nepodařilo se načíst cíle pro speciální notifikaci:", err);

            // Snažíme se z chybové odpovědi vytáhnout čitelnou zprávu,
            // ale pokud není k dispozici, použijeme obecnou.
            const message =
                err?.response?.data?.message ||
                err?.message ||
                "Nepodařilo se načíst možné příjemce speciální zprávy.";

            setError(message);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        if (autoLoad) {
            loadTargets();
        }
    }, [autoLoad, loadTargets]);

    return {
        targets,
        loading,
        error,
        reload: loadTargets,
    };
};