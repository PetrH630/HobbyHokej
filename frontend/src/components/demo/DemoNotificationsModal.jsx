// src/components/demo/DemoNotificationsModal.jsx
import "./DemoNotificationsModal.css";
import { useGlobalModal } from "../../hooks/useGlobalModal";


const DemoNotificationsModal = ({
    show,
    onClose,
    notifications,
    loading = false,
    error = null,
}) => {
    if (!show) return null;
  

    const emails = notifications?.emails ?? [];
    const sms = notifications?.sms ?? [];

    return (
        <>
            {/* Klik mimo obsah zavře modal */}
            <div
                className="modal fade show d-block"
                tabIndex="-1"
                role="dialog"
                onClick={onClose}
            >
                <div
                    className="modal-dialog modal-lg"
                    role="document"
                    onClick={(e) => e.stopPropagation()}
                >
                    <div className="modal-content demo-modal">
                        <div className="modal-header demo-modal-header">
                            <h5 className="modal-title">
                                DEMO - odeslané notifikace - pouze zobrazení - email se neodesílá
                            </h5>
                            <button
                                type="button"
                                className="btn-close"
                                onClick={onClose}
                                aria-label="Zavřít"
                            />
                        </div>

                        <div className="modal-body">
                            {loading && <p>Načítám notifikace…</p>}
                            {error && (
                                <div className="alert alert-danger">
                                    {error}
                                </div>
                            )}

                            {!loading && !error && (
                                <>
                                    {emails.length === 0 &&
                                        sms.length === 0 && (
                                            <p>
                                                Žádné demo notifikace k
                                                zobrazení.
                                            </p>
                                        )}

                                    {emails.length > 0 && (
                                        <>
                                            <h6>E-maily</h6>
                                            <div className="list-group mb-3">
                                                {emails.map((mail, idx) => {
                                                    let demoPrefixHtml = "";

                                                    if (
                                                        mail.type ===
                                                        "FORGOTTEN_PASSWORD_RESET_COMPLETED"
                                                    ) {
                                                        demoPrefixHtml = `
                                                            <div style="
                                                                background:#fff3cd;
                                                                border:1px solid #ffeeba;
                                                                padding:10px;
                                                                margin-bottom:10px;
                                                                border-radius:6px;
                                                                font-weight:600;
                                                            ">
                                                                ⚠️ DEMO režim - změna hesla u tohoto uživatele byla pouze simulována. U uživatele, kterého si sami vytvoříte bude heslo opravdu změněno.
                                                            </div>
                                                        `;
                                                    }

                                                    if (
                                                        mail.type ===
                                                        "USER_CREATED"
                                                    ) {
                                                        demoPrefixHtml = `
                                                            <div style="
                                                                background:#fff3cd;
                                                                border:1px solid #ffeeba;
                                                                padding:10px;
                                                                margin-bottom:10px;
                                                                border-radius:6px;
                                                                font-weight:600;
                                                            ">
                                                                ⚠️ DEMO režim - registrace uživatele byla úspěšně provedena. Aktivační e-mail nebyl ve skutečnosti odeslán. Účet je možné aktivovat pouze u uživatelů, které si sami vytvoříte.
                                                            </div>
                                                        `;
                                                    }
                                                    if (mail.type === "USER_ACTIVATED") {
                                                        demoPrefixHtml = `
                                                            <div style="
                                                                background:#fff3cd;
                                                                border:1px solid #ffeeba;
                                                                padding:10px;
                                                                margin-bottom:10px;
                                                                border-radius:6px;
                                                                font-weight:600;
                                                            ">
                                                                ⚠️ DEMO režim - registrace uživatele byla úspěšně provedena. Aktivační e-mail nebyl ve skutečnosti odeslán. Nyní se můžete přihlásit.
                                                            </div>
                                                        `;
                                                    }

                                                    return (
                                                        <div
                                                            key={mail.id ?? idx}
                                                            className="list-group-item"
                                                        >
                                                            <div className="small text-muted mb-1">
                                                                Typ:{" "}
                                                                <strong>
                                                                    {mail.type ??
                                                                        "-"}
                                                                </strong>{" "}
                                                                | Kanál:{" "}
                                                                <strong>
                                                                    {mail.recipientKind ??
                                                                        "-"}
                                                                </strong>
                                                            </div>
                                                            <div>
                                                                <strong>Komu:</strong>{" "}
                                                                {mail.to ?? "-"}
                                                            </div>
                                                            <div>
                                                                <strong>
                                                                    Předmět:
                                                                </strong>{" "}
                                                                {mail.subject ??
                                                                    "-"}
                                                            </div>
                                                            <div className="mt-2">
                                                                <strong>Text:</strong>
                                                                <div
                                                                    className="border rounded p-2 bg-light mt-1"
                                                                    style={{
                                                                        maxHeight:
                                                                            "200px",
                                                                        overflow:
                                                                            "auto",
                                                                    }}
                                                                    dangerouslySetInnerHTML={{
                                                                        __html:
                                                                            demoPrefixHtml +
                                                                            (mail.body ??
                                                                                ""),
                                                                    }}
                                                                />
                                                            </div>
                                                        </div>
                                                    );
                                                })}
                                            </div>
                                        </>
                                    )}

                                    {sms.length > 0 && (
                                        <>
                                            <h6>SMS</h6>
                                            <div className="list-group">
                                                {sms.map((s, idx) => (
                                                    <div
                                                        key={s.id ?? idx}
                                                        className="list-group-item"
                                                    >
                                                        <div className="small text-muted mb-1">
                                                            Typ:{" "}
                                                            <strong>
                                                                {s.type ?? "-"}
                                                            </strong>
                                                        </div>
                                                        <div>
                                                            <strong>Komu:</strong>{" "}
                                                            {s.to ?? "-"}
                                                        </div>
                                                        <div className="mt-2">
                                                            <strong>Text:</strong>
                                                            <div
                                                                className="border rounded p-2 bg-light mt-1"
                                                                style={{
                                                                    maxHeight:
                                                                        "400px",
                                                                    overflow:
                                                                        "auto",
                                                                    whiteSpace:
                                                                        "pre-wrap",
                                                                }}
                                                            >
                                                                {s.text ?? ""}
                                                            </div>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        </>
                                    )}
                                </>
                            )}
                        </div>

                        <div className="modal-footer">
                            <button
                                type="button"
                                className="btn btn-secondary"
                                onClick={onClose}
                            >
                                Zavřít
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Backdrop */}
            <div className="modal-backdrop fade show" onClick={onClose} />
        </>
    );
};

export default DemoNotificationsModal;
