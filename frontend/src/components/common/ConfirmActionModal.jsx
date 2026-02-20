// src/components/common/ConfirmActionModal
import React from "react";

const ConfirmActionModal = ({
    show,
    title,
    message,
    confirmText = "Potvrdit",
    confirmVariant = "primary",
    onConfirm,
    onClose,
}) => {
    if (!show) return null;

    return (
        <>
            <div className="modal fade show d-block" tabIndex="-1">
                <div className="modal-dialog modal-dialog-centered">
                    <div className="modal-content">

                        <div className="modal-header">
                            <h5 className="modal-title">{title}</h5>
                            <button
                                type="button"
                                className="btn-close"
                                onClick={onClose}
                            />
                        </div>

                        <div className="modal-body">
                            <p className="mb-0">{message}</p>
                        </div>

                        <div className="modal-footer">
                            <button
                                type="button"
                                className="btn btn-secondary"
                                onClick={onClose}
                            >
                                Zrušit
                            </button>

                            <button
                                type="button"
                                className={`btn btn-${confirmVariant}`}
                                onClick={onConfirm}
                            >
                                {confirmText}
                            </button>
                        </div>

                    </div>
                </div>
            </div>

            {/* backdrop */}
            <div className="modal-backdrop fade show"></div>
        </>
    );
};

export default ConfirmActionModal;
