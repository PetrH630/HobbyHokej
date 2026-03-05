// src/components/admin/CancelMatchModal.jsx

import { useState, useEffect } from "react";
import { MATCH_CANCEL_REASON_OPTIONS } from "../../constants/matchCancelReason";

/**
 * CancelMatchModal
 *
 * Bootstrap modal komponenta pro práci s modálním dialogem v aplikaci.
 *
 * Umožňuje zavření stiskem klávesy Escape.
 *
 * Props:
 * @param {import("../../types/dto").MatchDTO} props.match Data vybraného zápasu načtená z backendu.
 * @param {boolean} props.show určuje, zda je dialog otevřený.
 * @param {Function} props.onClose callback pro předání akce do nadřazené vrstvy.
 * @param {Function} props.onConfirm callback pro předání akce do nadřazené vrstvy.
 * @param {boolean} props.saving Příznak, že probíhá ukládání a akce mají být dočasně blokovány.
 * @param {string} props.serverError Chybová zpráva vrácená ze serveru.
 */

const CancelMatchModal = ({
    match,
    show,
    onClose,
    onConfirm,
    saving,
    serverError,
}) => {
    const [reason, setReason] = useState("");
    const [error, setError] = useState(null);

    useEffect(() => {
        if (show) {
            setReason("");
            setError(null);
        }
    }, [show]);

    if (!show || !match) {
        return null;
    }

    
/**
 * Naformátuje datum a čas do čitelné podoby pro potvrzovací dialog.
 */

const formatDateTime = (dt) => {
        if (!dt) return "-";
        try {
            const safe = dt.replace(" ", "T");
            const d = new Date(safe);
            if (Number.isNaN(d.getTime())) return dt;
            return d.toLocaleString("cs-CZ");
        } catch {
            return dt;
        }
    };

    
/**
 * Zpracuje odeslání formuláře a zavolá příslušný callback nadřazené komponenty.
 */

const handleSubmit = (e) => {
        e.preventDefault();

        if (!reason) {
            setError("Vyberte důvod zrušení zápasu.");
            return;
        }

        onConfirm(match.id, reason);
    };

    const handleChangeReason = (e) => {
        setReason(e.target.value);
        setError(null);
    };

    return (
        <>
            <div className="modal fade show d-block" tabIndex="-1">
                <div className="modal-dialog">
                    <div className="modal-content">
                        <form onSubmit={handleSubmit} noValidate>
                            <div className="modal-header">
                                <h5 className="modal-title">
                                    Zrušit zápas #{match.id}
                                </h5>
                                <button
                                    type="button"
                                    className="btn-close"
                                    onClick={onClose}
                                    disabled={saving}
                                />
                            </div>

                            <div className="modal-body">
                                <p className="mb-2">
                                    Opravdu chceš zrušit zápas?
                                </p>
                                <p className="small text-muted mb-3">
                                    Datum a čas:{" "}
                                    <strong>
                                        {formatDateTime(match.dateTime)}
                                    </strong>
                                    <br />
                                    Místo:{" "}
                                    <strong>{match.location || "-"}</strong>
                                </p>

                                {serverError && (
                                    <div className="alert alert-danger">
                                        {serverError}
                                    </div>
                                )}

                                <div className="mb-3">
                                    <label className="form-label">
                                        Důvod zrušení
                                    </label>
                                    <select
                                        className={
                                            "form-select" +
                                            (error ? " is-invalid" : "")
                                        }
                                        value={reason}
                                        onChange={handleChangeReason}
                                    >
                                        <option value="">
                                            -- vyberte důvod --
                                        </option>
                                        {MATCH_CANCEL_REASON_OPTIONS.map(
                                            (opt) => (
                                                <option
                                                    key={opt.value}
                                                    value={opt.value}
                                                >
                                                    {opt.label}
                                                </option>
                                            )
                                        )}
                                    </select>
                                    {error && (
                                        <div className="invalid-feedback">
                                            {error}
                                        </div>
                                    )}
                                </div>
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
                                <button
                                    type="submit"
                                    className="btn btn-danger"
                                    disabled={saving}
                                >
                                    {saving
                                        ? "Ruším zápas…"
                                        : "Zrušit zápas"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            <div className="modal-backdrop fade show" />
        </>
    );
};

export default CancelMatchModal;
