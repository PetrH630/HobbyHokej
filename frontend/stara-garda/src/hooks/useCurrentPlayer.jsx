// src/hooks/useCurrentPlayer.js
import { createContext, useContext, useEffect, useState } from "react";
import { getCurrentPlayer } from "../api/playerApi";

const CurrentPlayerContext = createContext(null);

export const CurrentPlayerProvider = ({ children }) => {
    const [currentPlayer, setCurrentPlayer] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null); // volitelné, ale hodí se

    const refreshCurrentPlayer = async () => {
        setLoading(true);
        setError(null);

        try {
            const data = await getCurrentPlayer();   //  přímo data
            setCurrentPlayer(data);                  // PlayerDTO nebo null
        } catch (err) {
            console.error("Nepodařilo se načíst aktuálního hráče", err);
            setCurrentPlayer(null);

            const message =
                err?.response?.data?.message ||
                err?.message ||
                "Nepodařilo se načíst aktuálního hráče";

            setError(message);
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
                setCurrentPlayer,      // setter z useState – můžeš ho dál používat ručně
                refreshCurrentPlayer,
                loading,
                error,                 // teď máš k dispozici i error
            }}
        >
            {children}
        </CurrentPlayerContext.Provider>
    );
};

export const useCurrentPlayer = () => {
    const ctx = useContext(CurrentPlayerContext);
    if (!ctx) {
        throw new Error("useCurrentPlayer musí být použit uvnitř CurrentPlayerProvider");
    }
    return ctx;
};
