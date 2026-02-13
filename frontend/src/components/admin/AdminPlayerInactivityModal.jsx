// src/components/admin/AdminPlayerInactivityModal.jsx
import { useEffect, useMemo, useState } from "react";
import {
    getInactivityByPlayerAdmin,
    createInactivityAdmin,
    updateInactivityAdmin,
    deleteInactivityAdmin,
} from "../../api/playerInactivityApi";
import { useNotification } from "../../context/NotificationContext";
import { useGlobalModal } from "../../hooks/useGlobalModal";

const toLocalInputValue = (isoOrNull) => {
    if (!isoOrNull) return "";
    const safe = isoOrNull.includes("T") ? isoOrNull : isoOrNull.replace(" ", "T");
    return safe.slice(0, 16); // YYYY-MM-DDTHH:mm
};

const parseLocalDateTime = (val) => {
    if (!val) return null;
    const d = new Date(val); // datetime-local je ve formátu "YYYY-MM-DDTHH:mm"
    return Number.isNaN(d.getTime()) ? null : d;
};

const AdminPlayerInactivityModal = ({ player, onClose, onSaved }) => {
    useGlobalModal(true);

    
    const { showNotification } = useNotification();

    const [periods, setPeriods] = useState([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);

    const [editing, setEditing] = useState(null);
    const [formValues, setFormValues] = useState({
        inactiveFrom: "",
        inactiveTo: "",
        inactivityReason: "",
    });

    const [touched, setTouched] = useState({
        inactiveFrom: false,
        inactiveTo: false,
        inactivityReason: false,
    });
    const [fieldErrors, setFieldErrors] = useState({
        inactiveFrom: "",
        inactiveTo: "",
        inactivityReason: "",
    });

    useEffect(() => {
        if (!player?.id) return;

        const load = async () => {
            try {
                setLoading(true);
                setError(null);
                const data = await getInactivityByPlayerAdmin(player.id);
                setPeriods(data || []);
            } catch (err) {
                console.error(err);
                setError(
                    err?.response?.data?.message ||
                    "Nepodařilo se načíst období neaktivity hráče."
                );
            } finally {
                setLoading(false);
            }
        };

        load();
    }, [player?.id]);

    const resetValidation = () => {
        setTouched({
            inactiveFrom: false,
            inactiveTo: false,
            inactivityReason: false,
        });
        setFieldErrors({
            inactiveFrom: "",
            inactiveTo: "",
            inactivityReason: "",
        });
    };

    const startCreate = () => {
        setEditing(null);
        setFormValues({
            inactiveFrom: "",
            inactiveTo: "",
            inactivityReason: "",
        });
        resetValidation();
    };

    const startEdit = (period) => {
        setEditing(period);
        setFormValues({
            inactiveFrom: toLocalInputValue(period.inactiveFrom),
            inactiveTo: toLocalInputValue(period.inactiveTo),
            inactivityReason: period.inactivityReason || "",
        });
        resetValidation();
    };

    const handleChange = (e) => {
        const { name, value } = e.target;

        setFormValues((prev) => ({ ...prev, [name]: value }));
        // průběžně maž chybu daného pole
        setFieldErrors((prev) => ({ ...prev, [name]: "" }));
    };

    const handleBlur = (e) => {
        const { name } = e.target;
        setTouched((prev) => ({ ...prev, [name]: true }));
    };

    const validate = useMemo(() => {
        return (values, allPeriods, editingPeriod) => {
            const nextErrors = {
                inactiveFrom: "",
                inactiveTo: "",
                inactivityReason: "",
            };

            const from = values.inactiveFrom?.trim() || "";
            const to = values.inactiveTo?.trim() || "";
            const reason = values.inactivityReason?.trim() || "";

            if (!from) nextErrors.inactiveFrom = "Vyplňte začátek neaktivity.";
            if (!to) nextErrors.inactiveTo = "Vyplňte konec neaktivity.";

            const fromDate = parseLocalDateTime(from);
            const toDate = parseLocalDateTime(to);

            if (from && !fromDate) nextErrors.inactiveFrom = "Neplatný formát data/času.";
            if (to && !toDate) nextErrors.inactiveTo = "Neplatný formát data/času.";

            if (fromDate && toDate && fromDate >= toDate) {
                nextErrors.inactiveTo = "Konec musí být po začátku.";
            }

            if (reason && reason.length > 255) {
                nextErrors.inactivityReason = "Maximální délka je 255 znaků.";
            }

            // překryvy s existujícími obdobími (až když jsou obě hodnoty validní)
            if (fromDate && toDate && !nextErrors.inactiveFrom && !nextErrors.inactiveTo) {
                const overlaps = (aFrom, aTo, bFrom, bTo) => aFrom < bTo && bFrom < aTo;

                const hasOverlap = (allPeriods || []).some((p) => {
                    if (editingPeriod && p.id === editingPeriod.id) return false;

                    const pFrom = parseLocalDateTime(toLocalInputValue(p.inactiveFrom));
                    const pTo = parseLocalDateTime(toLocalInputValue(p.inactiveTo));
                    if (!pFrom || !pTo) return false;

                    return overlaps(fromDate, toDate, pFrom, pTo);
                });

                if (hasOverlap) {
                    nextErrors.inactiveFrom = "Období se překrývá s existujícím záznamem.";
                    nextErrors.inactiveTo = "Období se překrývá s existujícím záznamem.";
                }
            }

            return nextErrors;
        };
    }, []);

    const hasAnyError = (errs) => Object.values(errs).some((v) => !!v);

    const handleSubmit = async (e) => {
        e.preventDefault();

        // označ pole jako touched, aby se zobrazily chyby
        setTouched({
            inactiveFrom: true,
            inactiveTo: true,
            inactivityReason: true,
        });

        const errs = validate(formValues, periods, editing);
        setFieldErrors(errs);

        if (hasAnyError(errs)) {
            showNotification("Zkontrolujte prosím vyplněná pole.", "warning");
            return;
        }

        try {
            setSaving(true);
            setError(null);

            const payload = {
                playerId: player.id,
                inactiveFrom: formValues.inactiveFrom,
                inactiveTo: formValues.inactiveTo,
                inactivityReason: formValues.inactivityReason?.trim()
                    ? formValues.inactivityReason.trim()
                    : null,
            };

            let result;
            if (editing) {
                result = await updateInactivityAdmin(editing.id, payload);
                showNotification("Období neaktivity bylo upraveno.", "success");
            } else {
                result = await createInactivityAdmin(payload);
                showNotification("Období neaktivity bylo vytvořeno.", "success");
            }

            setPeriods((prev) =>
                editing ? prev.map((p) => (p.id === result.id ? result : p)) : [...prev, result]
            );

            setEditing(null);
            setFormValues({
                inactiveFrom: "",
                inactiveTo: "",
                inactivityReason: "",
            });
            resetValidation();

            onSaved && onSaved();
        } catch (err) {
            console.error(err);
            setError(
                err?.response?.data?.message || "Uložení období neaktivity se nezdařilo."
            );
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm("Opravdu chceš smazat toto období neaktivity?")) return;

        try {
            setSaving(true);
            setError(null);
            await deleteInactivityAdmin(id);
            showNotification("Období neaktivity bylo smazáno.", "success");

            setPeriods((prev) => prev.filter((p) => p.id !== id));
            onSaved && onSaved();
        } catch (err) {
            console.error(err);
            setError(
                err?.response?.data?.message || "Smazání období neaktivity se nezdařilo."
            );
        } finally {
            setSaving(false);
        }
    };

    const formatDateTime = (dt) => {
        if (!dt) return "";
        const d = new Date(dt.includes("T") ? dt : dt.replace(" ", "T"));
        return Number.isNaN(d.getTime()) ? dt : d.toLocaleString("cs-CZ");
    };

    const inputClass = (name) => {
        const base = "form-control";
        if (!touched[name]) return base;
        return fieldErrors[name] ? `${base} is-invalid` : `${base} is-valid`;
    };

    return (
        <div className="modal d-block" tabIndex="-1">
            <div className="modal-dialog modal-lg">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">
                            Neaktivita hráče – {player.name} {player.surname}
                        </h5>
                        <button
                            type="button"
                            className="btn-close"
                            onClick={onClose}
                            disabled={saving}
                        />
                    </div>

                    <div className="modal-body">
                        {loading && <p>Načítám období neaktivity…</p>}
                        {error && <div className="alert alert-danger">{error}</div>}

                        {!loading && (
                            <>
                                <h6 className="mb-2">
                                    {editing
                                        ? "Upravit období neaktivity"
                                        : "Přidat nové období neaktivity"}
                                </h6>

                                <form className="row g-3 mb-4" onSubmit={handleSubmit} noValidate>
                                    <div className="col-md-6">
                                        <label className="form-label">Začátek</label>
                                        <input
                                            type="datetime-local"
                                            name="inactiveFrom"
                                            className={inputClass("inactiveFrom")}
                                            value={formValues.inactiveFrom}
                                            onChange={handleChange}
                                            onBlur={handleBlur}
                                            disabled={saving}
                                            required
                                        />
                                        {touched.inactiveFrom && fieldErrors.inactiveFrom && (
                                            <div className="invalid-feedback">
                                                {fieldErrors.inactiveFrom}
                                            </div>
                                        )}
                                    </div>

                                    <div className="col-md-6">
                                        <label className="form-label">Konec</label>
                                        <input
                                            type="datetime-local"
                                            name="inactiveTo"
                                            className={inputClass("inactiveTo")}
                                            value={formValues.inactiveTo}
                                            onChange={handleChange}
                                            onBlur={handleBlur}
                                            disabled={saving}
                                            required
                                        />
                                        {touched.inactiveTo && fieldErrors.inactiveTo && (
                                            <div className="invalid-feedback">
                                                {fieldErrors.inactiveTo}
                                            </div>
                                        )}
                                    </div>

                                    <div className="col-12">
                                        <label className="form-label">
                                            Důvod neaktivity (volitelné)
                                        </label>
                                        <input
                                            type="text"
                                            name="inactivityReason"
                                            className={inputClass("inactivityReason")}
                                            value={formValues.inactivityReason}
                                            onChange={handleChange}
                                            onBlur={handleBlur}
                                            disabled={saving}
                                            maxLength={255}
                                        />
                                        {touched.inactivityReason && fieldErrors.inactivityReason && (
                                            <div className="invalid-feedback">
                                                {fieldErrors.inactivityReason}
                                            </div>
                                        )}
                                        {!fieldErrors.inactivityReason && (
                                            <div className="form-text">
                                                Maximálně 255 znaků.
                                            </div>
                                        )}
                                    </div>

                                    <div className="col-12 d-flex gap-2">
                                        <button
                                            type="submit"
                                            className="btn btn-primary"
                                            disabled={saving}
                                        >
                                            {saving ? "Ukládám…" : editing ? "Uložit změny" : "Přidat období"}
                                        </button>

                                        {editing && (
                                            <button
                                                type="button"
                                                className="btn btn-secondary"
                                                onClick={startCreate}
                                                disabled={saving}
                                            >
                                                Zrušit úpravu
                                            </button>
                                        )}
                                    </div>
                                </form>

                                <hr />

                                <h6>Existující období</h6>
                                {periods.length === 0 && (
                                    <p className="text-muted">Hráč nemá žádná období neaktivity.</p>
                                )}

                                {periods.length > 0 && (
                                    <div className="table-responsive">
                                        <table className="table table-sm table-striped">
                                            <thead>
                                                <tr>
                                                    <th>Od</th>
                                                    <th>Do</th>
                                                    <th>Důvod</th>
                                                    <th className="text-end">Akce</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {periods.map((p) => (
                                                    <tr key={p.id}>
                                                        <td>{formatDateTime(p.inactiveFrom)}</td>
                                                        <td>{formatDateTime(p.inactiveTo)}</td>
                                                        <td>
                                                            {p.inactivityReason ? (
                                                                p.inactivityReason
                                                            ) : (
                                                                <span className="text-muted">
                                                                    neuveden
                                                                </span>
                                                            )}
                                                        </td>
                                                        <td className="text-end">
                                                            <button
                                                                className="btn btn-sm btn-outline-primary me-2"
                                                                onClick={() => startEdit(p)}
                                                                disabled={saving}
                                                            >
                                                                Upravit
                                                            </button>
                                                            <button
                                                                className="btn btn-sm btn-outline-danger"
                                                                onClick={() => handleDelete(p.id)}
                                                                disabled={saving}
                                                            >
                                                                Smazat
                                                            </button>
                                                        </td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>
                                    </div>
                                )}
                            </>
                        )}
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
    );
};

export default AdminPlayerInactivityModal;
