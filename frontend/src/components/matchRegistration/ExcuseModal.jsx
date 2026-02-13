// src/components/matchRegistraion/ExcuseModal.jsx
import React from "react";
import { EXCUSE_REASON_OPTIONS } from "../../constants/excuseReason";
import { useGlobalModal } from "../../hooks/useGlobalModal";


const ExcuseModal = ({
    show,
    saving = false,
    isUnregisterFlow = false,
    selectedReason,
    excuseNote,
    onChangeReason,
    onChangeNote,
    onClose,
    onSubmit,
}) => {
 

    if (!show) return null;

    const modalTitle = isUnregisterFlow
        ? "Odhlásit se ze zápasu"
        : "Omluvit se ze zápasu";

    const submitButtonClass = isUnregisterFlow ? "btn btn-info" : "btn btn-warning";

    const submitButtonText = isUnregisterFlow
        ? "Odeslat odhlášení"
        : "Odeslat omluvu";

    const handleBackdropClick = () => {
        if (!saving) onClose?.();
    };

    const stopPropagation = (e) => e.stopPropagation();

    return (
        <>
            <div
                className="modal fade show d-block"
                tabIndex="-1"
                role="dialog"
                aria-modal="true"
                onClick={handleBackdropClick}
            >
                <div className="modal-dialog" role="document" onClick={stopPropagation}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title">{modalTitle}</h5>
                            <button
                                type="button"
                                className="btn-close"
                                onClick={onClose}
                                disabled={saving}
                                aria-label="Zavřít"
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
                                        onChange={(e) => onChangeReason?.(e.target.value)}
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
                                    onChange={(e) => onChangeNote?.(e.target.value)}
                                    disabled={saving}
                                />
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
                                type="button"
                                className={submitButtonClass}
                                onClick={onSubmit}
                                disabled={saving}
                            >
                                {submitButtonText}
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div className="modal-backdrop fade show" onClick={handleBackdropClick} />
        </>
    );
};

export default ExcuseModal;
