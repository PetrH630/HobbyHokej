// src/components/Players.jsx
import { usePlayers } from "../../hooks/usePlayers";
import PlayerCard from "./PlayerCard";
import { setCurrentPlayer } from "../../api/playerApi";
import { useNavigate } from "react-router-dom";
import { useCurrentPlayer } from "../../hooks/useCurrentPlayer";

import "./Players.css";

const Players = () => {
    const { players, loading, error } = usePlayers();
    const navigate = useNavigate();

    const { currentPlayer, refreshCurrentPlayer } = useCurrentPlayer();

    const handleSelectPlayer = async (playerId) => {
        try {
            await setCurrentPlayer(playerId);
            await refreshCurrentPlayer();
            navigate("/app/matches");
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
                    onClick={() => navigate("/app/createPlayer")}
                >
                    Vytvořit hráče
                </button>
            </div>
        );
    }

    return (
        <div className="container mt-3">
            <div className="player-list">
                {players.map((p) => {
                    const isActive = currentPlayer?.id === p.id;

                    const disabledTooltip =
                        p.playerStatus === "PENDING"
                            ? "Hráč čeká na schválení administrátorem"
                            : p.playerStatus === "REJECTED"
                                ? "Hráč byl zamítnut administrátorem"
                                : "";

                    return (
                        <div className="player-item" key={p.id}>
                            <div
                                className={`selected-player-frame ${isActive ? "selected-player-frame--on" : ""
                                    }`}
                                aria-label={isActive ? "Vybraný hráč" : undefined}
                            >
                                {isActive && (
                                    <div className="selected-player-badge">
                                        Vybraný hráč
                                    </div>
                                )}

                                <PlayerCard
                                    player={p}
                                    // ✅ necháme false, aby se nerušily rámečky
                                    isActive={false}
                                    onSelect={() => handleSelectPlayer(p.id)}
                                    disabledTooltip={disabledTooltip}
                                />
                            </div>
                        </div>
                    );
                })}
            </div>

            <div className="text-center mt-4">
                <button
                    className="btn btn-outline-primary"
                    onClick={() => navigate("/app/createPlayer")}
                >
                    Přidat dalšího hráče
                </button>
            </div>
        </div>
    );
};

export default Players;
