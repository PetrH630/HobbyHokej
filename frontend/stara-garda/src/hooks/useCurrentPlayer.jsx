// src/hooks/useCurrentPlayer.js
import { createContext, useContext, useEffect, useState } from "react";
import { getCurrentPlayer } from "../api/playerApi";

const CurrentPlayerContext = createContext(null);

export const CurrentPlayerProvider = ({ children }) => {
    const [currentPlayer, setCurrentPlayer] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const refreshCurrentPlayer = async () => {
        setLoading(true);
        setError(null);

        try {
            const data = await getCurrentPlayer();   // PlayerDTO nebo null
            setCurrentPlayer(data);
            return data;
        } catch (err) {
            console.error("Nepodařilo se načíst aktuálního hráče", err);
            setCurrentPlayer(null);

            const message =
                err?.response?.data?.message ||
                err?.message ||
                "Nepodařilo se načíst aktuálního hráče";

            setError(message);
            return null;
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        // při prvním načtení chráněné části webu se zkusí načíst aktuální hráč
        refreshCurrentPlayer();
    }, []);

    return (
        <CurrentPlayerContext.Provider
            value={{
                currentPlayer,
                setCurrentPlayer,
                refreshCurrentPlayer,
                loading,
                error,
            }}
        >
            {children}
        </CurrentPlayerContext.Provider>
    );
};

export const useCurrentPlayer = () => {
    const ctx = useContext(CurrentPlayerContext);

    // 👇 DŮLEŽITÁ ZMĚNA – místo throw vrátíme „safe fallback“
    if (!ctx) {
        return {
            currentPlayer: null,
            setCurrentPlayer: () => { },
            refreshCurrentPlayer: async () => null,
            loading: false,
            error: null,
        };
    }

    return ctx;
};
