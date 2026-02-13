// src/hooks/useCurrentPlayer.js
import { createContext, useContext, useEffect, useState } from "react";
import {
    getCurrentPlayer,
    getMyPlayers,
    setCurrentPlayer,
} from "../api/playerApi";

const CurrentPlayerContext = createContext(null);

export const CurrentPlayerProvider = ({ children }) => {
    const [currentPlayer, setCurrentPlayerState] = useState(null);
    const [players, setPlayers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    /**
     * Načte kontext aktuálního hráče:
     * - seznam hráčů přihlášeného uživatele
     * - aktuálně zvoleného hráče (pokud je nastaven)
     *
     * Pokud není aktuální hráč nastaven a uživatel má přesně jednoho hráče,
     * tento hráč se automaticky nastaví jako aktuální.
     */
    const refreshCurrentPlayer = async () => {
        setLoading(true);
        setError(null);

        try {
            // Načteme moje hráče a aktuálního hráče paralelně
            const [playersData, currentPlayerData] = await Promise.all([
                getMyPlayers(),     // GET /players/me
                getCurrentPlayer(), // GET /current-player
            ]);

            setPlayers(playersData || []);

            if (currentPlayerData) {
                // Backend má nastaveného aktuálního hráče
                setCurrentPlayerState(currentPlayerData);
                return currentPlayerData;
            }

            // Aktuální hráč není nastaven
            if (playersData && playersData.length === 1) {
                // Pokud má uživatel právě jednoho hráče, automaticky ho nastavíme
                const only = playersData[0];
                await setCurrentPlayer(only.id); // POST /current-player/{playerId}
                setCurrentPlayerState(only);
                return only;
            }

            // 0 hráčů nebo více hráčů bez auto výběru – necháme currentPlayer null
            setCurrentPlayerState(null);
            return null;
        } catch (err) {
            console.error("Nepodařilo se načíst kontext aktuálního hráče", err);
            setCurrentPlayerState(null);
            setPlayers([]);

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
        // při prvním načtení chráněné části webu se zkusí načíst kontext hráčů
        refreshCurrentPlayer();
    }, []);

    /**
     * Změní aktuálního hráče na základě ID.
     *
     * Volá se z frontendu (např. z dropdownu v Navbaru) a nastaví hráče
     * jak v backendu, tak lokálně v kontextu.
     */
    const changeCurrentPlayer = async (playerId) => {
        if (!playerId) {
            return;
        }

        await setCurrentPlayer(playerId); // POST /current-player/{playerId}
        const found = players.find((p) => p.id === playerId) || null;
        setCurrentPlayerState(found);
    };

    return (
        <CurrentPlayerContext.Provider
            value={{
                currentPlayer,
                setCurrentPlayer: setCurrentPlayerState, // pro případné ruční použití
                players,
                changeCurrentPlayer,
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

    // „safe fallback“ když provider náhodou neobklopuje komponentu
    if (!ctx) {
        return {
            currentPlayer: null,
            setCurrentPlayer: () => { },
            players: [],
            changeCurrentPlayer: async () => { },
            refreshCurrentPlayer: async () => null,
            loading: false,
            error: null,
        };
    }

    return ctx;
};
