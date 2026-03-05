import { useEffect } from "react";
import { useGlobalModal } from "../../hooks/useGlobalModal";

/**
 * SuccessModal
 *
 * Bootstrap modal komponenta pro práci s modálním dialogem v aplikaci.
 *
 * Umožňuje zavření stiskem klávesy Escape.
 * Při otevření blokuje scroll pozadí pomocí useGlobalModal.
 *
 * Props:
 * @param {boolean} props.show určuje, zda je dialog otevřený.
 * @param {string} props.title titulek zobrazený v hlavičce. [default: "Hotovo"]
 * @param {string} props.message obsah zprávy zobrazený uživateli.
 * @param {Function} props.onClose callback pro předání akce do nadřazené vrstvy.
 * @param {Object} props.closeLabel vstupní hodnota komponenty. [default: "Zavřít"]
 */
const SuccessModal = ({
    show,
    title = "Hotovo",
    message,
    onClose,
    closeLabel = "Zavřít",
}) => {

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

            <div className="modal-backdrop fade show" />

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
