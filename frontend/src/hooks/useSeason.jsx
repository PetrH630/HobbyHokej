// src/hooks/useSeason.jsx
import { createContext, useContext, useEffect, useState } from "react";
import {
    fetchSeasonsForUser,
    fetchCurrentSeasonForUser,
    setCurrentSeasonForUser,
} from "../api/seasonApi";
import { useAuth } from "./useAuth";

const SeasonContext = createContext(null);

export const SeasonProvider = ({ children }) => {
    const { user } = useAuth();
    const [seasons, setSeasons] = useState([]);
    const [currentSeasonId, setCurrentSeasonId] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const loadSeasons = async () => {
            if (!user) {
                setSeasons([]);
                setCurrentSeasonId(null);
                return;
            }

            setLoading(true);
            setError(null);

            try {
                const [allSeasons, currentSeason] = await Promise.all([
                    fetchSeasonsForUser(),
                    fetchCurrentSeasonForUser(),
                ]);

                console.log("Načtené sezóny:", allSeasons);
                console.log("Aktuální sezóna:", currentSeason);

                setSeasons(allSeasons || []);

                if (currentSeason && currentSeason.id) {
                    setCurrentSeasonId(currentSeason.id);
                } else if (allSeasons && allSeasons.length > 0) {
                    // když není nastavená aktuální sezóna, můžeš defaultně vybrat první
                    setCurrentSeasonId(allSeasons[0].id);
                } else {
                    setCurrentSeasonId(null);
                }
            } catch (e) {
                console.error("Failed to load seasons", e);
                setError("Nepodařilo se načíst seznam sezón.");
            } finally {
                setLoading(false);
            }
        };

        loadSeasons();
    }, [user]);

    const changeSeason = async (seasonId) => {
        try {
            setLoading(true);
            setError(null);
            await setCurrentSeasonForUser(seasonId);
            setCurrentSeasonId(seasonId);
        } catch (e) {
            console.error("Failed to change season", e);
            setError("Nepodařilo se změnit sezónu.");
        } finally {
            setLoading(false);
        }
    };

    const value = {
        seasons,
        currentSeasonId,
        changeSeason,
        loading,
        error,
    };

    return (
        <SeasonContext.Provider value={value}>
            {children}
        </SeasonContext.Provider>
    );
};

export const useSeason = () => {
    const ctx = useContext(SeasonContext);
    if (!ctx) {
        throw new Error("useSeason musí být použit uvnitř SeasonProvider");
    }
    return ctx;
};
