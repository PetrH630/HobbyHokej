// src/hooks/usePostLoginRedirect.jsx
import { useNavigate } from "react-router-dom";
import { autoSelectCurrentPlayer, getCurrentPlayer } from "../api/playerApi";

/**
 * Hook, který po přihlášení:
 * 1) zavolá backend /current-player/auto-select
 * 2) zjistí aktuálního hráče
 * 3) přesměruje na /matches nebo /players
 */
const usePostLoginRedirect = () => {
    const navigate = useNavigate();

    const run = async () => {
        try {
            // 1) auto-select na backendu (může nic nevybrat – to nevadí)
            await autoSelectCurrentPlayer();
        } catch (err) {
            console.error("Auto-select aktuálního hráče selhal", err);
            // chyba nás nezastaví – dál prostě zjistíme currentPlayer
        }

        try {
            // 2) zjistíme, jestli je po auto-selectu nastaven currentPlayer
            const player = await getCurrentPlayer(); // PlayerDTO nebo null

            // 3) redirect podle výsledku
            if (player) {
                navigate("/matches");
            } else {
                navigate("/players");
            }
        } catch (err) {
            console.error("Nepodařilo se zjistit aktuálního hráče", err);
            // když ani tohle nevyjde, pošleme usera aspoň na players
            navigate("/players");
        }
    };

    return run;
};

export default usePostLoginRedirect;
