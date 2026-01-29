// src/pages/MatchDetailPage.jsx
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { useState } from "react";

import { useMatchDetail } from "../hooks/useMatchDetail";
import { upsertMyRegistration } from "../api/matchRegistrationApi";
import MatchDetail from "../components/matches/MatchDetail";
import { useNotification } from "../context/NotificationContext";
import { useCurrentPlayer } from "../hooks/useCurrentPlayer";
import { ExcuseReason, EXCUSE_REASON_OPTIONS } from "../constants/excuseReason";

const MatchDetailPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();

    const { match, loading, error } = useMatchDetail(id);
    const [saving, setSaving] = useState(false);
    const [actionError, setActionError] = useState(null);

    const { showNotification } = useNotification();
    const { currentPlayer } = useCurrentPlayer();

    const playerName = currentPlayer?.fullName || "Hráč";

    // jestli jde o uplynulý zápas (přijde z router state)
    const isPast = location.state?.isPast === true;

    // sjednocení s backendem – pojmenování přesně jako DTO
    const playerMatchStatus = match?.playerMatchStatus ?? "NO_RESPONSE";
    const matchStatus = match?.matchStatus ?? null;      // např. SCHEDULED / CANCELED / PLAYED

    // stav pro modál (společný pro omluvu i odhlášení)
    const [showExcuseModal, setShowExcuseModal] = useState(false);
    const [selectedReason, setSelectedReason] = useState(ExcuseReason.JINE);
    const [excuseNote, setExcuseNote] = useState("");
    const [isUnregisterFlow, setIsUnregisterFlow] = useState(false); // true = řešíme odhlášení

    const handleRegister = async () => {
        if (!match) return;
        try {
            setSaving(true);
            setActionError(null);

            await upsertMyRegistration({
                matchId: match.id,
                team: null,
                excuseReason: null,
                excuseNote: null,
                unregister: false,
            });

            showNotification(
                `${playerName} byl úspěšně přihlášen na zápas.`,
                "success"
            );
            navigate("/matches");
        } catch (err) {
            console.error(err);
            setActionError(
                err?.response?.data?.message ||
                "Nepodařilo se přihlásit na zápas."
            );
        } finally {
            setSaving(false);
        }
    };

    // handler pro "Možná" / SUBSTITUTE
    const handleSubstitute = async () => {
        if (!match) return;
        try {
            setSaving(true);
            setActionError(null);

            await upsertMyRegistration({
                matchId: match.id,
                team: null,
                excuseReason: null,
                excuseNote: null,
                unregister: false,   // není odhlášení
                substitute: true,    // KLÍČOVÉ – backend z toho udělá SUBSTITUTE
            });

            showNotification(
                `${playerName} se přihlásil jako náhradník - možná příjde).`,
                "info"
            );
            navigate("/matches");
        } catch (err) {
            console.error(err);
            setActionError(
                err?.response?.data?.message ||
                "Nepodařilo se nastavit stav 'Možná'."
            );
        } finally {
            setSaving(false);
        }
    };

    // společná funkce pro otevření modálu (omluva / odhlášení)
    const openModal = (isUnregister) => {
        if (!match) return;

        setActionError(null);
        setIsUnregisterFlow(isUnregister);

        // předvyplnit z backendu, pokud už existuje omluva
        const backendReason = match.excuseReason;
        const backendNote = match.excuseNote;

        setSelectedReason(
            backendReason && ExcuseReason[backendReason]
                ? backendReason
                : ExcuseReason.JINE
        );

        setExcuseNote(
            backendNote ??
            (isUnregister
                ? "Odhlášení ze zápasu."
                : "Nemohu se zúčastnit.")
        );

        setShowExcuseModal(true);
    };

    // místo přímého odhlášení jen otevřeme modál v režimu "unregister"
    const handleUnregister = () => {
        openModal(true);
    };

    // otevření modálu pro klasickou omluvu
    const openExcuseModal = () => {
        openModal(false);
    };

    // odeslání z modálu – podle režimu buď omluva, nebo odhlášení s důvodem
    const submitModalAction = async () => {
        if (!match) return;

        try {
            setSaving(true);
            setActionError(null);

            await upsertMyRegistration({
                matchId: match.id,
                team: null,
                excuseReason: selectedReason,
                excuseNote,
                unregister: isUnregisterFlow,
            });

            if (isUnregisterFlow) {
                showNotification(
                    `${playerName} byl odhlášen ze zápasu.`,
                    "info"
                );
            } else {
                showNotification(
                    `${playerName} se omluvil ze zápasu.`,
                    "warning"
                );
            }

            setShowExcuseModal(false);
            setIsUnregisterFlow(false);
            navigate("/matches");
        } catch (err) {
            console.error(err);
            setActionError(
                err?.response?.data?.message ||
                (isUnregisterFlow
                    ? "Nepodařilo se odhlásit ze zápasu."
                    : "Nepodařilo se omluvit ze zápasu.")
            );
        } finally {
            setSaving(false);
        }
    };

    const handleCloseModal = () => {
        setShowExcuseModal(false);
        setIsUnregisterFlow(false);
    };

    const modalTitle = isUnregisterFlow
        ? "Odhlásit se ze zápasu"
        : "Omluvit se ze zápasu";

    const submitButtonClass = isUnregisterFlow
        ? "btn btn-info"
        : "btn btn-warning";

    const submitButtonText = isUnregisterFlow
        ? "Odeslat odhlášení"
        : "Odeslat omluvu";

    return (
        <>
            <MatchDetail
                match={match}
                playerMatchStatus={playerMatchStatus}  // jednoznačný název
                matchStatus={matchStatus}             // stav zápasu Cancel/
                loading={loading}
                error={error}
                actionError={actionError}
                onRegister={handleRegister}
                onUnregister={handleUnregister}
                onExcuse={openExcuseModal}
                onSubstitute={handleSubstitute}
                saving={saving}
                isPast={isPast}
            />

            {showExcuseModal && (
                <div
                    className="modal fade show d-block"
                    tabIndex="-1"
                    role="dialog"
                >
                    <div className="modal-dialog" role="document">
                        <div className="modal-content">
                            <div className="modal-header">
                                <h5 className="modal-title">{modalTitle}</h5>
                                <button
                                    type="button"
                                    className="btn-close"
                                    onClick={handleCloseModal}
                                    disabled={saving}
                                />
                            </div>

                            <div className="modal-body">
                                <p>Vyberte důvod:</p>

                                {EXCUSE_REASON_OPTIONS.map((opt) => (
                                    <div className="form-check" key={opt.value}>
                                        <input
                                            className="form-check-input"
                                            type="radio"
                                            name="excuseReason"
                                            id={`excuse-${opt.value}`}
                                            value={opt.value}
                                            checked={selectedReason === opt.value}
                                            onChange={(e) =>
                                                setSelectedReason(e.target.value)
                                            }
                                            disabled={saving}
                                        />
                                        <label
                                            className="form-check-label"
                                            htmlFor={`excuse-${opt.value}`}
                                        >
                                            {opt.label}
                                        </label>
                                    </div>
                                ))}

                                <div className="mt-3">
                                    <label className="form-label">
                                        Poznámka (volitelné):
                                    </label>
                                    <textarea
                                        className="form-control"
                                        rows={3}
                                        value={excuseNote}
                                        onChange={(e) =>
                                            setExcuseNote(e.target.value)
                                        }
                                        disabled={saving}
                                    />
                                </div>
                            </div>

                            <div className="modal-footer">
                                <button
                                    type="button"
                                    className="btn btn-secondary"
                                    onClick={handleCloseModal}
                                    disabled={saving}
                                >
                                    Zavřít
                                </button>
                                <button
                                    type="button"
                                    className={submitButtonClass}
                                    onClick={submitModalAction}
                                    disabled={saving}
                                >
                                    {submitButtonText}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
};

export default MatchDetailPage;
