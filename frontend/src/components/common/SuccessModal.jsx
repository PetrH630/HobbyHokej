//src/components/common/SuccessModal.jsx
import { useEffect } from "react";
import { useGlobalModal } from "../../hooks/useGlobalModal";

/**
 * Univerzální informační modal pro potvrzení úspěšné akce.
 *
 * Zobrazuje nadpis a zprávu. Zavření je možné tlačítkem i klávesou Escape.
 */
const SuccessModal = ({
    show,
    title = "Hotovo",
    message,
    onClose,
    closeLabel = "Zavřít",
}) => {
    // zamkne scroll body při otevřeném modalu (sjednocení chování s ostatními modaly)
    useGlobalModal(show === true);

    useEffect(() => {
        if (!show) return;

        const onKeyDown = (e) => {
            if (e.key === "Escape") onClose?.();
        };

        window.addEventListener("keydown", onKeyDown);
        return () => window.removeEventListener("keydown", onKeyDown);
    }, [show, onClose]);

    if (!show) return null;

    return (
        <>
            {/* Backdrop */}
            <div className="modal-backdrop fade show" />

            {/* Modal */}
            <div className="modal d-block" tabIndex="-1" role="dialog" aria-modal="true">
                <div className="modal-dialog modal-dialog-centered">
                    <div className="modal-content shadow">
                        <div className="modal-header">
                            <h5 className="modal-title">{title}</h5>
                            <button
                                type="button"
                                className="btn-close"
                                onClick={onClose}
                                aria-label="Zavřít"
                            />
                        </div>

                        <div className="modal-body">
                            <div className="alert alert-success mb-0">
                                {message}
                            </div>
                        </div>

                        <div className="modal-footer">
                            <button type="button" className="btn btn-primary" onClick={onClose}>
                                {closeLabel}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default SuccessModal;
