// src/components/notifications/AdminSpecialNotificationModal.jsx
import { useEffect, useMemo, useState } from "react";
import { useSpecialNotificationTargets } from "../../hooks/useSpecialNotificationTargets";
import { sendSpecialNotification } from "../../api/notificationsApi";

/**
 * Modal pro odeslání speciální zprávy vybraným uživatelům / hráčům.
 *
 * Vlastnosti:
 * - Načítá možné cíle přes /api/notifications/admin/special/targets.
 * - Umožňuje výběr více cílů (checkboxy, včetně "Vybrat vše").
 * - Odesílá speciální zprávu přes /api/notifications/admin/special.
 * - V DEMO režimu zobrazuje náhled zachycených e-mailů a SMS.
 *
 * @param {Object} props
 * @param {boolean} props.show Určuje, zda je modal zobrazen.
 * @param {Function} props.onClose Handler pro zavření modalu.
 * @param {Function} [props.onSent] Volitelný callback po úspěšném odeslání (payload, demoResult).
 */
const AdminSpecialNotificationModal = ({ show, onClose, onSent }) => {
    const { targets, loading, error, reload } = useSpecialNotificationTargets(show);

    const [selectedKeys, setSelectedKeys] = useState(new Set());

    const [title, setTitle] = useState("");
    const [message, setMessage] = useState("");
    const [sendEmail, setSendEmail] = useState(true);
    const [sendSms, setSendSms] = useState(false);

    const [submitError, setSubmitError] = useState(null);
    const [submitSuccess, setSubmitSuccess] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    const [demoPreview, setDemoPreview] = useState(null);

    // příznak, že odeslání proběhlo úspěšně – potom už nepůjde znovu odeslat
    const [sent, setSent] = useState(false);

    // Pomocná funkce pro jednoznačný klíč cíle
    const makeKey = (target) =>
        `${target.userId ?? "null"}-${target.playerId ?? "null"}`;

    // Reset stavu při zavření/otevření modalu
    useEffect(() => {
        if (!show) {
            // Po zavření modal kompletně vyčistit stav
            setSelectedKeys(new Set());
            setTitle("");
            setMessage("");
            setSendEmail(true);
            setSendSms(false);
            setSubmitError(null);
            setSubmitSuccess(null);
            setDemoPreview(null);
            setSent(false);
        } else {
            // Při otevření jen zrušit případné staré hlášky
            setSubmitError(null);
            setSubmitSuccess(null);
            setDemoPreview(null);
            setSent(false);
        }
    }, [show]);

    // Odvozené seznamy
    const selectedTargets = useMemo(
        () => targets.filter((t) => selectedKeys.has(makeKey(t))),
        [targets, selectedKeys]
    );

    const allSelected =
        targets.length > 0 &&
        targets.every((t) => selectedKeys.has(makeKey(t)));

    const hasSelection = selectedTargets.length > 0;

    const isValidForm =
        title.trim().length > 0 &&
        message.trim().length > 0 &&
        hasSelection &&
        !submitting &&
        !sent; // po úspěšném odeslání už nepůjde znovu odeslat

    const handleToggleOne = (target) => {
        const key = makeKey(target);
        setSelectedKeys((prev) => {
            const next = new Set(prev);
            if (next.has(key)) {
                next.delete(key);
            } else {
                next.add(key);
            }
            return next;
        });
    };

    const handleToggleAll = () => {
        if (allSelected) {
            // Zrušit vše
            setSelectedKeys(new Set());
        } else {
            // Vybrat vše
            const all = new Set(targets.map((t) => makeKey(t)));
            setSelectedKeys(all);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        setSubmitError(null);
        setSubmitSuccess(null);
        setDemoPreview(null);

        if (!hasSelection) {
            setSubmitError("Vyber prosím alespoň jednoho příjemce.");
            return;
        }

        if (!title.trim() || !message.trim()) {
            setSubmitError("Vyplň prosím nadpis i text zprávy.");
            return;
        }

        const payload = {
            title: title.trim(),
            message: message.trim(),
            sendEmail,
            sendSms,
            targets: selectedTargets.map((t) => ({
                userId: t.userId,
                playerId: t.playerId, // může být null
            })),
        };

        setSubmitting(true);
        try {
            const demoResult = await sendSpecialNotification(payload);

            if (demoResult) {
                // DEMO režim – backend vrací DemoNotificationsDTO
                setDemoPreview(demoResult);
                setSubmitSuccess(
                    "Zpráva byla odeslána (DEMO režim – e-maily a SMS se fyzicky neodeslaly)."
                );
            } else {
                // Produkce – 204 No Content
                setSubmitSuccess("Zpráva byla odeslána vybraným příjemcům.");
            }

            setSent(true);

            if (typeof onSent === "function") {
                onSent(payload, demoResult ?? null);
            }
        } catch (err) {
            console.error("Chyba při odesílání speciální zprávy:", err);
            const message =
                err?.response?.data?.message ||
                err?.message ||
                "Nepodařilo se odeslat speciální zprávu.";
            setSubmitError(message);
        } finally {
            setSubmitting(false);
        }
    };

    const handleCloseClick = () => {
        if (submitting) {
            return;
        }
        onClose && onClose();
    };

    if (!show) {
        return null;
    }

    // Pokusíme se vytáhnout první e-mail z DemoNotificationsDTO pro pěkný náhled
    const emailPreview =
        demoPreview &&
            Array.isArray(demoPreview.emails) &&
            demoPreview.emails.length > 0
            ? demoPreview.emails[0]
            : null;

    return (
        <>
            <div
                className="modal fade show d-block"
                tabIndex="-1"
                role="dialog"
                aria-modal="true"
            >
                <div className="modal-dialog modal-xl modal-dialog-scrollable" role="document">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title">
                                Odeslat speciální zprávu
                            </h5>
                            <button
                                type="button"
                                className="btn-close"
                                aria-label="Close"
                                onClick={handleCloseClick}
                                disabled={submitting}
                            />
                        </div>

                        <div className="modal-body">
                            {/* Chyby z načítání cílů */}
                            {error && (
                                <div className="alert alert-danger">
                                    {error}
                                    <button
                                        type="button"
                                        className="btn btn-sm btn-outline-light ms-2"
                                        onClick={reload}
                                    >
                                        Zkusit znovu načíst příjemce
                                    </button>
                                </div>
                            )}

                            {/* Chyba při odeslání */}
                            {submitError && (
                                <div className="alert alert-danger">
                                    {submitError}
                                </div>
                            )}

                            {/* Úspěch */}
                            {submitSuccess && (
                                <div className="alert alert-success">
                                    {submitSuccess}
                                </div>
                            )}

                            <div className="row">
                                {/* Seznam příjemců */}
                                <div className="col-12 col-lg-6 mb-3">
                                    <div className="d-flex justify-content-between align-items-center mb-2">
                                        <h6 className="mb-0">Příjemci</h6>
                                        <div className="form-check">
                                            <input
                                                className="form-check-input"
                                                type="checkbox"
                                                id="special-select-all"
                                                checked={allSelected}
                                                onChange={handleToggleAll}
                                                disabled={loading || targets.length === 0 || sent}
                                            />
                                            <label
                                                className="form-check-label"
                                                htmlFor="special-select-all"
                                            >
                                                Vybrat vše
                                            </label>
                                        </div>
                                    </div>

                                    {loading && (
                                        <p>Načítám seznam příjemců…</p>
                                    )}

                                    {!loading && targets.length === 0 && (
                                        <p className="text-muted">
                                            Není k dispozici žádný možný příjemce.
                                        </p>
                                    )}

                                    {!loading && targets.length > 0 && (
                                        <div
                                            className="border rounded p-2"
                                            style={{ maxHeight: "300px", overflowY: "auto" }}
                                        >
                                            {targets.map((target) => {
                                                const key = makeKey(target);
                                                const checked = selectedKeys.has(key);
                                                return (
                                                    <div
                                                        key={key}
                                                        className="form-check d-flex align-items-center py-1"
                                                    >
                                                        <input
                                                            className="form-check-input me-2"
                                                            type="checkbox"
                                                            id={`special-target-${key}`}
                                                            checked={checked}
                                                            onChange={() =>
                                                                handleToggleOne(target)
                                                            }
                                                            disabled={submitting || sent}
                                                        />
                                                        <label
                                                            className="form-check-label d-flex flex-column flex-sm-row w-100"
                                                            htmlFor={`special-target-${key}`}
                                                        >
                                                            <span className="me-2">
                                                                {target.displayName}
                                                            </span>
                                                            <span className="badge bg-secondary ms-sm-auto mt-1 mt-sm-0">
                                                                {target.type === "PLAYER"
                                                                    ? "Hráč"
                                                                    : "Uživatel"}
                                                            </span>
                                                        </label>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    )}

                                    <div className="mt-2 small text-muted">
                                        Vybráno příjemců: {selectedTargets.length}
                                    </div>
                                </div>

                                {/* Formulář zprávy */}
                                <div className="col-12 col-lg-6 mb-3">
                                    <form onSubmit={handleSubmit}>
                                        <div className="mb-3">
                                            <label
                                                htmlFor="special-title"
                                                className="form-label"
                                            >
                                                Nadpis zprávy
                                            </label>
                                            <input
                                                type="text"
                                                id="special-title"
                                                className="form-control"
                                                value={title}
                                                onChange={(e) =>
                                                    setTitle(e.target.value)
                                                }
                                                disabled={submitting || sent}
                                                maxLength={200}
                                                placeholder="Např. Změna času tréninku"
                                            />
                                        </div>

                                        <div className="mb-3">
                                            <label
                                                htmlFor="special-message"
                                                className="form-label"
                                            >
                                                Text zprávy
                                            </label>
                                            <textarea
                                                id="special-message"
                                                className="form-control"
                                                rows={6}
                                                value={message}
                                                onChange={(e) =>
                                                    setMessage(e.target.value)
                                                }
                                                disabled={submitting || sent}
                                                placeholder="Sem napiš text, který se zobrazí jako in-app notifikace, v e-mailu i v SMS (podle zvolených kanálů)."
                                            />
                                        </div>

                                        <div className="mb-3">
                                            <div className="form-check">
                                                <input
                                                    className="form-check-input"
                                                    type="checkbox"
                                                    id="special-send-email"
                                                    checked={sendEmail}
                                                    onChange={(e) =>
                                                        setSendEmail(
                                                            e.target.checked
                                                        )
                                                    }
                                                    disabled={submitting || sent}
                                                />
                                                <label
                                                    className="form-check-label"
                                                    htmlFor="special-send-email"
                                                >
                                                    Odeslat také e-mailem
                                                </label>
                                            </div>
                                            <div className="form-check">
                                                <input
                                                    className="form-check-input"
                                                    type="checkbox"
                                                    id="special-send-sms"
                                                    checked={sendSms}
                                                    onChange={(e) =>
                                                        setSendSms(
                                                            e.target.checked
                                                        )
                                                    }
                                                    disabled={submitting || sent}
                                                />
                                                <label
                                                    className="form-check-label"
                                                    htmlFor="special-send-sms"
                                                >
                                                    Odeslat také SMS
                                                </label>
                                            </div>
                                            <div className="form-text">
                                                In-app notifikace se vždy uloží,
                                                bez ohledu na volbu e-mailu / SMS.
                                            </div>
                                        </div>

                                        <div className="d-flex justify-content-end gap-2">
                                            <button
                                                type="button"
                                                className="btn btn-outline-secondary"
                                                onClick={handleCloseClick}
                                                disabled={submitting}
                                            >
                                                Zavřít
                                            </button>
                                            <button
                                                type="submit"
                                                className="btn btn-primary"
                                                disabled={!isValidForm}
                                            >
                                                {submitting
                                                    ? "Odesílám…"
                                                    : sent
                                                        ? "Zpráva odeslána"
                                                        : "Odeslat zprávu"}
                                            </button>
                                        </div>
                                    </form>
                                </div>
                            </div>

                            {/* DEMO náhled (pokud backend vrátí DemoNotificationsDTO) */}
                            {demoPreview && (
                                <div className="mt-3">
                                    <h6>Zpráva byla odeslána (DEMO náhled)</h6>

                                    {emailPreview && (
                                        <>
                                            <div className="small text-muted mb-2">
                                                Ukázka e-mailu tak, jak ho uvidí příjemci.
                                            </div>
                                            <div className="border rounded bg-light p-2 mb-3">
                                                {emailPreview.subject && (
                                                    <div className="fw-semibold mb-2">
                                                        {emailPreview.subject}
                                                    </div>
                                                )}
                                                {/* pokud je obsah HTML, vykreslíme ho jako HTML */}
                                                {emailPreview.html ? (
                                                    <div
                                                        className="bg-white p-2 rounded"
                                                        dangerouslySetInnerHTML={{
                                                            __html: emailPreview.body,
                                                        }}
                                                    />
                                                ) : (
                                                    <pre className="bg-white p-2 rounded mb-0">
                                                        {emailPreview.body}
                                                    </pre>
                                                )}
                                            </div>
                                        </>
                                    )}

                                    {selectedTargets.length > 0 && (
                                        <div>
                                            <div className="fw-semibold mb-1">
                                                Příjemci:
                                            </div>
                                            <ul className="small mb-0">
                                                {selectedTargets.map((t) => (
                                                    <li key={makeKey(t)}>
                                                        {t.displayName}
                                                    </li>
                                                ))}
                                            </ul>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Bootstrap backdrop */}
            <div
                className="modal-backdrop fade show"
                onClick={handleCloseClick}
            />
        </>
    );
};

export default AdminSpecialNotificationModal;