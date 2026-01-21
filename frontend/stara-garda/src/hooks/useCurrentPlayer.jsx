// src/hooks/useCurrentPlayer.js
import { createContext, useContext, useEffect, useState } from "react";
import { getCurrentPlayer } from "../api/playerApi";

const CurrentPlayerContext = createContext(null);

export const CurrentPlayerProvider = ({ children }) => {
    const [currentPlayer, setCurrentPlayer] = useState(null);
    const [loading, setLoading] = useState(true);

    const refreshCurrentPlayer = async () => {
        setLoading(true);
        try {
            const res = await getCurrentPlayer();
            setCurrentPlayer(res.data);
        } catch (err) {
            console.error("Nepodařilo se načíst aktuálního hráče", err);
            setCurrentPlayer(null);
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
            value={{ currentPlayer, setCurrentPlayer, refreshCurrentPlayer, loading }}
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
