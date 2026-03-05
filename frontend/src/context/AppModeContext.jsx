import { createContext, useContext, useEffect, useState } from "react";
import { fetchAppMode } from "../api/modeApi";
import { useAuth } from "../hooks/useAuth";

const AppModeContext = createContext({
    demoMode: false,
    loading: true,
    error: null,
});

/**
 * AppModeContext
 *
 * React context zodpovědný za načtení a sdílení režimu aplikace (např. DEMO/PROD).
 * Kontext se po přihlášení uživatele pokusí načíst režim z backendu a ukládá výsledek do lokálního stavu.
 *
 * Poskytuje helper `refresh()` pro opětovné načtení stavu a `loading` pro řízení UI během inicializace.
 */
export const AppModeProvider = ({ children }) => {
    const [demoMode, setDemoMode] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Z auth contextu – POZOR: teď už existuje isAuthenticated
    const { isAuthenticated } = useAuth();

    console.log("[AppModeProvider] render", {
        demoMode,
        loading,
        error,
        isAuthenticated,
    });

    useEffect(() => {
        console.log(
            "[AppModeProvider] useEffect – auth změna, isAuthenticated:",
            isAuthenticated
        );

        // pokud uživatel není přihlášený:
        //  - nenačítáme nic z backendu
        //  - režim nastavíme na "false"
        if (!isAuthenticated) {
            setDemoMode(false);
            setLoading(false);
            setError(null);
            return;
        }

        // přihlášený uživatel → načteme režim z backendu
        const loadMode = async () => {
            try {
                setLoading(true);
                setError(null);

                console.log("[AppModeProvider] calling fetchAppMode() ...");

                const data = await fetchAppMode();

                console.log("[AppModeProvider] fetchAppMode() data:", data);

                setDemoMode(Boolean(data.demoMode));
            } catch (err) {
                console.error(
                    "[AppModeProvider] ERROR while loading app mode:",
                    err
                );

                let message = "Nepodařilo se načíst režim aplikace";

                if (err.response) {
                    console.error(
                        "[AppModeProvider] error response:",
                        err.response.status,
                        err.response.data
                    );
                    message = `Chyba serveru (${err.response.status})`;
                } else if (err.request) {
                    console.error(
                        "[AppModeProvider] no response, err.request:",
                        err.request
                    );
                    message = "Server neodpovídá";
                } else if (err.message) {
                    message = err.message;
                }

                setError(message);
                setDemoMode(false);
            } finally {
                setLoading(false);
                console.log("[AppModeProvider] useEffect FINISHED");
            }
        };

        loadMode();
    }, [isAuthenticated]); // 👈 refetch vždy, když se změní stav přihlášení

    return (
        <AppModeContext.Provider value={{ demoMode, loading, error }}>
            {children}
        </AppModeContext.Provider>
    );
};

export const useAppMode = () => useContext(AppModeContext);
