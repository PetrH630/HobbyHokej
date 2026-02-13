// src/hooks/useDemoMode.js

import { useAppMode } from "../context/AppModeContext";

/**
 * Custom hook pro práci s režimem aplikace (demo / produkce).
 *
 * Vrací:
 *  - demoMode: boolean (true = demo režim, false = produkce)
 *  - loading: boolean (probíhá načítání režimu)
 *  - error: string | null (chybová hláška, pokud selže načtení)
 *  - isDemo: alias na demoMode pro čitelnější použití
 */
export const useDemoMode = () => {
    const { demoMode, loading, error } = useAppMode();

    return {
        demoMode,
        loading,
        error,
        isDemo: Boolean(demoMode),
    };
};
