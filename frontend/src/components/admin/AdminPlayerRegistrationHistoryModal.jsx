// src/components/admin/AdminPlayerRegistrationHistoryModal.jsx
import React, { useState } from "react";
import AdminPlayerRegistrationHistory from "./AdminPlayerRegistrationHistory";
import { useGlobalModal } from "../../hooks/useGlobalModal";

const AdminPlayerRegistrationHistoryModal = ({
    match,
    player,
    saving,
    onClose,
    onMarkNoExcuse,
    onCancelNoExcuse,
}) => {

    if (!player || !match) return null;

    const registeredPlayers = match.registeredPlayers ?? [];
    const noExcusedPlayers = match.noExcusedPlayers ?? [];

    const isRegistered = registeredPlayers.some((p) => p.id === player.id);
    const isNoExcused = noExcusedPlayers.some((p) => p.id === player.id);

    const [showCancelNote, setShowCancelNote] = useState(false);
    const [cancelNote, setCancelNote] = useState(
        "Omluven - nakonec opravdu nemohl"
    );

    const handleMarkNoExcuseClick = () => {
        if (!onMarkNoExcuse) return;
        // poznámku můžeš kdykoli předělat na vlastní textarea
        onMarkNoExcuse("Admin: bez omluvy");
    };

    const handleStartCancel = () => {
        setShowCancelNote(true);
    };

    const handleConfirmCancel = () => {
        if (!onCancelNoExcuse) return;
        onCancelNoExcuse(cancelNote);
        setShowCancelNote(false);
    };

    const playerName = player.fullName ?? `${player.name} ${player.surname}`;

    return (
        <div className="modal d-block" tabIndex="-1">
            <div className="modal-dialog modal-lg">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">
                            Historie registrací – {playerName}
                        </h5>
                        <button
                            type="button"
                            className="btn-close"
                            onClick={onClose}
                            disabled={saving}
                        />
                    </div>
                    <div className="modal-body">
                        
                        <div className="mb-3">
                            {isRegistered && !isNoExcused && (
                                <button
                                    type="button"
                                    className="btn btn-sm btn-outline-danger me-2"
                                    disabled={saving}
                                    onClick={handleMarkNoExcuseClick}
                                >
                                    Označit jako bez omluvy
                                </button>
                            )}

                            {isNoExcused && (
                                <div className="d-flex flex-column flex-md-row align-items-start gap-2">
                                    {!showCancelNote && (
                                        <button
                                            type="button"
                                            className="btn btn-sm btn-outline-secondary"
                                            disabled={saving}
                                            onClick={handleStartCancel}
                                        >
                                            Zrušit neomluvení
                                        </button>
                                    )}

                                    {showCancelNote && (
                                        <div className="d-flex flex-column flex-md-row align-items-start gap-2 flex-grow-1">
                                            <textarea
                                                className="form-control"
                                                rows={2}
                                                value={cancelNote}
                                                onChange={(e) =>
                                                    setCancelNote(
                                                        e.target.value
                                                    )
                                                }
                                                disabled={saving}
                                            />
                                            <button
                                                type="button"
                                                className="btn btn-sm btn-primary mt-2 mt-md-0"
                                                disabled={saving}
                                                onClick={handleConfirmCancel}
                                            >
                                                Potvrdit
                                            </button>
                                        </div>
                                    )}
                                </div>
                            )}

                            {!isRegistered && !isNoExcused && (
                                <p className="text-muted mb-0">
                                    Pro tohoto hráče není aktuálně k dispozici
                                    akce „bez omluvy“ / „zrušit neomluvení“.
                                </p>
                            )}
                        </div>

                        {/* 🔹 Tabulka historie */}
                        <AdminPlayerRegistrationHistory
                            matchId={match.id}
                            playerId={player.id}
                        />
                    </div>
                    <div className="modal-footer">
                        <button
                            type="button"
                            className="btn btn-secondary"
                            onClick={onClose}
                            disabled={saving}
                        >
                            Zavřít
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminPlayerRegistrationHistoryModal;
