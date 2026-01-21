//logika oddělena od komponenty UI.
import { useState, useEffect } from "react";
import { getMyPlayers } from "../api/playerApi";

export const usePlayers = () => {
    const [players, setPlayers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        setError(null);
        setLoading(true);

        getMyPlayers()
            .then(res => setPlayers(res.data))
            .catch(err => {
                const msg =
                    err?.response?.data?.message ||
                    err.message ||
                    "Nepodařilo se načíst hráče";
                setError(msg);
            })
            .finally(() => setLoading(false));
    }, []);

    return { players, loading, error };
};
