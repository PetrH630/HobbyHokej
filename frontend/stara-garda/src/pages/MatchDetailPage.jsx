// src/pages/MatchDetailPage.jsx
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { useState } from "react";

import { useMatchDetail } from "../hooks/useMatchDetail";
import { upsertMyRegistration } from "../api/MatchRegistrationApi";
import MatchDetail from "../components/MatchDetail";
import { useNotification } from "../context/NotificationContext";
import { useCurrentPlayer } from "../hooks/useCurrentPlayer";
import { ExcuseReason, EXCUSE_REASON_OPTIONS } from "../constants/excuseReason";

const MatchDetailPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();

    const { match, loading, error, reload } = useMatchDetail(id);
    const [saving, setSaving] = useState(false);
    const [actionError, setActionError] = useState(null);

    const { showNotification } = useNotification();
    const { currentPlayer } = useCurrentPlayer();

    const playerName = currentPlayer?.fullName || "Hráč";

    // ⬇⬇⬇ jestli jde o uplynulý zápas (přijde z router state)
    const isPast = location.state?.isPast === true;

    // ⬇⬇⬇ status z backendu (MatchDetailDTO.status)
    const status = match?.status ?? "NO_RESPONSE";

    // stav pro modál omluvy
    const [showExcuseModal, setShowExcuseModal] = useState(false);
    const [selectedReason, setSelectedReason] = useState(ExcuseReason.JINE);
    const [excuseNote, setExcuseNote] = useState("Nemohu se zúčastnit.");

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

    const handleUnregister = async () => {
        if (!match) return;
        try {
            setSaving(true);
            setActionError(null);

            await upsertMyRegistration({
                matchId: match.id,
                team: null,
                excuseReason: null,
                excuseNote: null,
                unregister: true,
            });

            showNotification(
                `${playerName} byl odhlášen ze zápasu.`,
                "info"
            );
            navigate("/matches");
        } catch (err) {
            console.error(err);
            setActionError(
                err?.response?.data?.message ||
                "Nepodařilo se odhlásit ze zápasu."
            );
        } finally {
            setSaving(false);
        }
    };

    const openExcuseModal = () => {
        if (!match) return;
        setActionError(null);
        setShowExcuseModal(true);
    };

    const submitExcuse = async () => {
        if (!match) return;

        try {
            setSaving(true);
            setActionError(null);

            await upsertMyRegistration({
                matchId: match.id,
                team: null,
                excuseReason: selectedReason,
                excuseNote,
                unregister: false,
            });

            showNotification(
                `${playerName} se omluvil ze zápasu.`,
                "warning"
            );
            setShowExcuseModal(false);
            navigate("/matches");
        } catch (err) {
            console.error(err);
            setActionError(
                err?.response?.data?.message ||
                "Nepodařilo se omluvit ze zápasu."
            );
        } finally {
            setSaving(false);
        }
    };

    return (
        <>
            <MatchDetail
                match={match}
                status={status}          // ⬅ TADY DŮLEŽITÉ
                loading={loading}
                error={error}
                actionError={actionError}
                onRegister={handleRegister}
                onUnregister={handleUnregister}
                onExcuse={openExcuseModal}
                saving={saving}
                isPast={isPast}          // ⬅ a tady
            />

            {showExcuseModal && (
                <div className="modal fade show d-block" tabIndex="-1" role="dialog">
                    <div className="modal-dialog" role="document">
                        <div className="modal-content">
                            <div className="modal-header">
                                <h5 className="modal-title">Omluvit se ze zápasu</h5>
                                <button
                                    type="button"
                                    className="btn-close"
                                    onClick={() => setShowExcuseModal(false)}
                                    disabled={saving}
                                />
                            </div>

                            <div className="modal-body">
                                <p>Vyberte důvod omluvy:</p>

                                {EXCUSE_REASON_OPTIONS.map((opt) => (
                                    <div className="form-check" key={opt.value}>
                                        <input
                                            className="form-check-input"
                                            type="radio"
                                            name="excuseReason"
                                            id={`excuse-${opt.value}`}
                                            value={opt.value}
                                            checked={selectedReason === opt.value}
                                            onChange={(e) => setSelectedReason(e.target.value)}
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
                                        onChange={(e) => setExcuseNote(e.target.value)}
                                        disabled={saving}
                                    />
                                </div>
                            </div>

                            <div className="modal-footer">
                                <button
                                    type="button"
                                    className="btn btn-secondary"
                                    onClick={() => setShowExcuseModal(false)}
                                    disabled={saving}
                                >
                                    Zavřít
                                </button>
                                <button
                                    type="button"
                                    className="btn btn-warning"
                                    onClick={submitExcuse}
                                    disabled={saving}
                                >
                                    Odeslat omluvu
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
