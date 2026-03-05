// src/components/admin/AdminPlayerRegistrationHistoryModal.jsx
import React, { useState } from "react";
import AdminPlayerRegistrationHistory from "./AdminPlayerRegistrationHistory";
import { useGlobalModal } from "../../hooks/useGlobalModal";
// TODO: modaly stejně jako u hráče
import ConfirmActionModal from "../common/ConfirmActionModal";
import SuccessModal from "../common/SuccessModal";

/**
 * AdminPlayerRegistrationHistoryModal
 *
 * Bootstrap modal komponenta pro práci s modálním dialogem v aplikaci.
 *
 * Při otevření blokuje scroll pozadí pomocí useGlobalModal.
 *
 * Props:
 * @param {import("../../types/dto").MatchDTO} props.match Data vybraného zápasu načtená z backendu.
 * @param {import("../../types/dto").PlayerDTO} props.player Data hráče používaná pro zobrazení nebo administraci.
 * @param {boolean} props.saving Příznak, že probíhá ukládání a akce mají být dočasně blokovány.
 * @param {Function} props.onClose callback pro předání akce do nadřazené vrstvy.
 * @param {Object} props.onMarkNoExcuse vstupní hodnota komponenty.
 * @param {Object} props.onCancelNoExcuse vstupní hodnota komponenty.
 * @param {Object} props.onChangeTeam vstupní hodnota komponenty.
 */

const AdminPlayerRegistrationHistoryModal = ({
    match,
    player,
    saving,
    onClose,
    onMarkNoExcuse,
    onCancelNoExcuse,
    // callback z AdminMatchInfo – provede reálnou změnu týmu
    onChangeTeam, // TODO
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

    // TODO: stav pro ConfirmActionModal a SuccessModal
    const [showConfirmChangeTeam, setShowConfirmChangeTeam] = useState(false);
    const [showSuccessModal, setShowSuccessModal] = useState(false);
    const [successMessage, setSuccessMessage] = useState("");

    const handleMarkNoExcuseClick = () => {
        if (!onMarkNoExcuse) return;
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

    // zjistíme, v jakém týmu hráč je, abychom mohli aspoň přibližně ukázat cílový tým
    const darkPlayers = match.registeredDarkPlayers ?? [];
    const lightPlayers = match.registeredLightPlayers ?? [];

    const isInDark = darkPlayers.some((p) => p.id === player.id);
    const isInLight = lightPlayers.some((p) => p.id === player.id);

    const targetTeamText = isInDark
        ? "LIGHT"
        : isInLight
            ? "DARK"
            : "druhý tým"; // fallback, kdyby nebyl v žádném z nich

    // klik na „Změnit tým“ – jen otevře confirm modal
    const handleChangeTeamClick = () => {
        setShowConfirmChangeTeam(true);
    };

    const handleConfirmChangeTeam = async () => {
        if (typeof onChangeTeam !== "function") {
            setShowConfirmChangeTeam(false);
            return;
        }

        try {
            await onChangeTeam(); // zavolá AdminMatchInfo handler
            setShowConfirmChangeTeam(false);
            setSuccessMessage(
                `Tým hráče byl úspěšně změněn na ${targetTeamText}.`
            );
            setShowSuccessModal(true);
        } catch (err) {
            // chyba se řeší v AdminMatchInfo přes notifikaci
            console.error("Chyba při admin změně týmu hráče:", err);
            setShowConfirmChangeTeam(false);
        }
    };

    const handleCloseSuccessModal = () => {
        setShowSuccessModal(false);
    };

    const playerName = player.fullName ?? `${player.name} ${player.surname}`;

    return (
        <>
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
                                    <>
                                        <button
                                            type="button"
                                            className="btn btn-sm btn-outline-danger me-2"
                                            disabled={saving}
                                            onClick={handleMarkNoExcuseClick}
                                        >
                                            Označit jako bez omluvy
                                        </button>
                                      
                                    </>
                                )}

                                {isNoExcused && (
                                    <div className="d-flex flex-column flex-md-row align-items-start gap-2">
                                        {!showCancelNote && (
                                            <>
                                                <button
                                                    type="button"
                                                    className="btn btn-sm btn-outline-secondary"
                                                    disabled={saving}
                                                    onClick={handleStartCancel}
                                                >
                                                    Zrušit neomluvení
                                                </button>

                                                {/* TODO: Změnit tým – můžeš mít dostupné i pro NO_EXCUSED */}
                                                <button
                                                    type="button"
                                                    className="btn btn-sm btn-outline-primary"
                                                    disabled={saving}
                                                    onClick={
                                                        handleChangeTeamClick
                                                    }
                                                >
                                                    Změnit tým
                                                </button>
                                            </>
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
                                                    className="btn btn-sm btn-primary mt-2 mt-md-0 text-nowrap"
                                                    disabled={saving}
                                                    onClick={
                                                        handleConfirmCancel
                                                    }
                                                >
                                                    Potvrdit
                                                </button>
                                            </div>
                                        )}
                                    </div>
                                )}

                                {!isRegistered && !isNoExcused && (
                                    <p className="text-muted mb-0">
                                        Pro tohoto hráče není aktuálně k
                                        dispozici akce „bez omluvy“ / „zrušit
                                        neomluvení“.
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

            {/* TODO: ConfirmActionModal pro změnu týmu */}
            <ConfirmActionModal
                show={showConfirmChangeTeam}
                title="Změna týmu hráče"
                message={`Opravdu chceš změnit tým hráče na ${targetTeamText}?`}
                confirmText="Změnit tým"
                confirmVariant="primary"
                onConfirm={handleConfirmChangeTeam}
                onClose={() => setShowConfirmChangeTeam(false)}
            />

            {/* TODO: SuccessModal po úspěšné změně týmu */}
            <SuccessModal
                show={showSuccessModal}
                title="Tým změněn"
                message={successMessage}
                onClose={handleCloseSuccessModal}
                closeLabel="OK"
            />
        </>
    );
};

export default AdminPlayerRegistrationHistoryModal;