// src/components/Player.jsx
// UI komponenta pověřená jen zobrazením (teď už i trochu logikou 🙂)
import { usePlayers } from "../hooks/usePlayers";
import PlayerCard from "./PlayerCard";
import { setCurrentPlayer } from "../api/playerApi";
import { useNavigate } from "react-router-dom";
import { useCurrentPlayer } from "../hooks/useCurrentPlayer";

import "./Player.css";

const Player = () => {
    const { players, loading, error } = usePlayers();
    const navigate = useNavigate();

    // ⬇⬇⬇ TADY PŘIDÁNO currentPlayer
    const { currentPlayer, refreshCurrentPlayer } = useCurrentPlayer();

    const handleSelectPlayer = async (playerId) => {
        try {
            await setCurrentPlayer(playerId);
            await refreshCurrentPlayer();  // znovu načte aktuálního hráče
            navigate("/matches");          // po úspěchu přechod na zápasy
        } catch (err) {
            console.error("Nelze nastavit aktuálního hráče", err);
            alert("Nepodařilo se vybrat hráče.");
        }
    };

    if (loading) return <p>Načítám hráče…</p>;
    if (error) return <p className="text-danger">{error}</p>;

    if (players.length === 0) {
        return (
            <div className="text-center mt-4">
                <p className="mb-3">
                    Ještě nemáte vytvořeného žádného hráče.
                    <br />
                    Chcete ho nyní vytvořit?
                </p>

                <button
                    className="btn btn-primary"
                    onClick={() => navigate("/createPlayer")}
                >
                    Vytvořit hráče
                </button>
            </div>
        );
    }
    return (
        <div className="container mt-3">

            {/* seznam hráčů */}
            <div className="player-list">
                {players.map((p) => (
                    <div className="player-item" key={p.id}>
                        <PlayerCard
                            player={p}
                            isActive={currentPlayer?.id === p.id}
                            onSelect={() => handleSelectPlayer(p.id)}
                        />
                    </div>
                ))}
            </div>

            {/* ➕ tlačítko pro přidání dalšího hráče */}
            <div className="text-center mt-4">
                <button
                    className="btn btn-outline-primary"
                    onClick={() => navigate("/createPlayer")}
                >
                Přidat dalšího hráče
                </button>
            </div>

        </div>
    );
};

export default Player;