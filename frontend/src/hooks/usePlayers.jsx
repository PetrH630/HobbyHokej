// logika oddělena od komponenty UI
import { useState, useEffect } from "react";
import { getMyPlayers } from "../api/playerApi";

export const usePlayers = () => {
    const [players, setPlayers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        let isMounted = true;

        const loadPlayers = async () => {
            setLoading(true);
            setError(null);

            try {
                const data = await getMyPlayers(); // přímo data
                if (isMounted) {
                    setPlayers(data);
                }
            } catch (err) {
                if (!isMounted) return;

                const message =
                    err?.response?.data?.message ||
                    err?.message ||
                    "Nepodařilo se načíst hráče";

                setError(message);
            } finally {
                if (isMounted) {
                    setLoading(false);
                }
            }
        };

        loadPlayers();

        return () => {
            isMounted = false;
        };
    }, []);

    return { players, loading, error };
};
