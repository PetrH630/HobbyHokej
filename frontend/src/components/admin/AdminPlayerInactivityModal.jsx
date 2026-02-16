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
import DatePicker from "../forms/DatePicker";

// z backendu "yyyy-MM-dd HH:mm:ss" nebo ISO -> "YYYY-MM-DD"
const toLocalDateValue = (value) => {
    if (!value) return "";
    const s = String(value);
    return s.slice(0, 10);
};

// "YYYY-MM-DD" -> Date (LOCAL, bez timezone posunu)
const parseLocalDate = (val) => {
    if (!val) return null;
    const s = String(val).trim();
    const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(s);
    if (!m) return null;
    const y = Number(m[1]);
    const mo = Number(m[2]) - 1;
    const d = Number(m[3]);
    const date = new Date(y, mo, d, 0, 0, 0, 0);
    return Number.isNaN(date.getTime()) ? null : date;
};

// "YYYY-MM-DD" -> "YYYY-MM-DDTHH:mm:ss" (LocalDateTime pro backend)

const toBackendStartOfDay = (dateStr) => (dateStr ? `${dateStr}T00:00:00` : null);
const toBackendEndOfDay = (dateStr) => (dateStr ? `${dateStr}T23:59:59` : null);


const AdminPlayerInactivityModal = ({ player, onClose, onSaved }) => {
    useGlobalModal(true);

    const { showNotification } = useNotification();

    const [periods, setPeriods] = useState([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);

    const [editing, setEditing] = useState(null);
    const [formValues, setFormValues] = useState({
        inactiveFrom: "", // "YYYY-MM-DD"
        inactiveTo: "",   // "YYYY-MM-DD"
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
            inactiveFrom: toLocalDateValue(period.inactiveFrom),
            inactiveTo: toLocalDateValue(period.inactiveTo),
            inactivityReason: period.inactivityReason || "",
        });
        resetValidation();
    };

    const handleTextChange = (e) => {
        const { name, value } = e.target;
        setFormValues((prev) => ({ ...prev, [name]: value }));
        setFieldErrors((prev) => ({ ...prev, [name]: "" }));
    };

    const handleBlur = (e) => {
        const { name } = e.target;
        setTouched((prev) => ({ ...prev, [name]: true }));
    };

    const handleDateChange = (name) => (valueString) => {
        setFormValues((prev) => ({ ...prev, [name]: valueString }));
        setFieldErrors((prev) => ({ ...prev, [name]: "" }));
    };

    const validate = useMemo(() => {
        return (values, allPeriods, editingPeriod) => {
            const nextErrors = {
                inactiveFrom: "",
                inactiveTo: "",
                inactivityReason: "",
            };

            const from = String(values.inactiveFrom ?? "").trim();
            const to = String(values.inactiveTo ?? "").trim();
            const reason = String(values.inactivityReason ?? "").trim();

            if (!from) nextErrors.inactiveFrom = "Vyplňte začátek neaktivity.";
            if (!to) nextErrors.inactiveTo = "Vyplňte konec neaktivity.";
            if (!reason) nextErrors.inactivityReason = "Důvod neaktivity je povinný.";

            const fromDate = parseLocalDate(from);
            const toDate = parseLocalDate(to);

            if (from && !fromDate) nextErrors.inactiveFrom = "Neplatný formát data.";
            if (to && !toDate) nextErrors.inactiveTo = "Neplatný formát data.";

            // date-only: povolíme stejný den (od 00:00 do 23:59:59)
            if (fromDate && toDate && fromDate > toDate) {
                nextErrors.inactiveTo = "Konec musí být stejný den nebo po začátku.";
            }

            if (reason && reason.length > 255) {
                nextErrors.inactivityReason = "Maximální délka je 255 znaků.";
            }

            // překryvy intervalů — porovnáváme date-only, ale logiku držíme jako [from, to]
            // protože do backendu jde do konce dne, překryv date-only je ok.
            if (fromDate && toDate && !nextErrors.inactiveFrom && !nextErrors.inactiveTo) {
                const overlaps = (aFrom, aTo, bFrom, bTo) => aFrom <= bTo && bFrom <= aTo;

                const hasOverlap = (allPeriods || []).some((p) => {
                    if (editingPeriod && p.id === editingPeriod.id) return false;

                    const pFrom = parseLocalDate(toLocalDateValue(p.inactiveFrom));
                    const pTo = parseLocalDate(toLocalDateValue(p.inactiveTo));
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
                inactiveFrom: toBackendStartOfDay(formValues.inactiveFrom), // ✅ 00:00:00
                inactiveTo: toBackendEndOfDay(formValues.inactiveTo),       // ✅ 23:59:59
                inactivityReason: formValues.inactivityReason.trim(),
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

            startCreate();
            onSaved && onSaved();
        } catch (err) {
            console.error(err);
            setError(err?.response?.data?.message || "Uložení období neaktivity se nezdařilo.");
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
            setError(err?.response?.data?.message || "Smazání období neaktivity se nezdařilo.");
        } finally {
            setSaving(false);
        }
    };

    const formatDate = (dt) => {
        const v = toLocalDateValue(dt);
        const d = parseLocalDate(v);
        return d ? d.toLocaleDateString("cs-CZ") : v;
    };

    // ✅ žádné zelené fajfky
    const inputClass = (name) => {
        const base = "form-control";
        if (!touched[name]) return base;
        return fieldErrors[name] ? `${base} is-invalid` : base;
    };

    return (
        <div className="modal d-block" tabIndex="-1">
            <div className="modal-dialog modal-lg">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">
                            Neaktivita hráče – {player.name} {player.surname}
                        </h5>
                        <button type="button" className="btn-close" onClick={onClose} disabled={saving} />
                    </div>

                    <div className="modal-body">
                        {loading && <p>Načítám období neaktivity…</p>}
                        {error && <div className="alert alert-danger">{error}</div>}

                        {!loading && (
                            <>
                                <h6 className="mb-2">
                                    {editing ? "Upravit období neaktivity" : "Přidat nové období neaktivity"}
                                </h6>

                                <form className="row g-3 mb-4" onSubmit={handleSubmit} noValidate>
                                    <div className="col-md-6">
                                        <label className="form-label" htmlFor="inactiveFrom">Začátek</label>
                                        <DatePicker
                                            id="inactiveFrom"
                                            name="inactiveFrom"
                                            value={formValues.inactiveFrom}
                                            onChange={handleDateChange("inactiveFrom")}
                                            onBlur={handleBlur}
                                            disabled={saving}
                                            className={inputClass("inactiveFrom")}
                                            required
                                        />
                                        {touched.inactiveFrom && fieldErrors.inactiveFrom && (
                                            <div className="invalid-feedback d-block">{fieldErrors.inactiveFrom}</div>
                                        )}
                                    </div>

                                    <div className="col-md-6">
                                        <label className="form-label" htmlFor="inactiveTo">Konec</label>
                                        <DatePicker
                                            id="inactiveTo"
                                            name="inactiveTo"
                                            value={formValues.inactiveTo}
                                            onChange={handleDateChange("inactiveTo")}
                                            onBlur={handleBlur}
                                            disabled={saving}
                                            className={inputClass("inactiveTo")}
                                            required
                                        />
                                        {touched.inactiveTo && fieldErrors.inactiveTo && (
                                            <div className="invalid-feedback d-block">{fieldErrors.inactiveTo}</div>
                                        )}
                                    </div>

                                    <div className="col-12">
                                        <label className="form-label">Důvod neaktivity</label>
                                        <input
                                            type="text"
                                            name="inactivityReason"
                                            className={inputClass("inactivityReason")}
                                            value={formValues.inactivityReason}
                                            onChange={handleTextChange}
                                            onBlur={handleBlur}
                                            disabled={saving}
                                            maxLength={255}
                                        />
                                        {touched.inactivityReason && fieldErrors.inactivityReason && (
                                            <div className="invalid-feedback d-block">{fieldErrors.inactivityReason}</div>
                                        )}
                                        {!fieldErrors.inactivityReason && (
                                            <div className="form-text">Maximálně 255 znaků.</div>
                                        )}
                                    </div>

                                    <div className="col-12 d-flex gap-2">
                                        <button type="submit" className="btn btn-primary" disabled={saving}>
                                            {saving ? "Ukládám…" : editing ? "Uložit změny" : "Přidat období"}
                                        </button>

                                        {editing && (
                                            <button type="button" className="btn btn-secondary" onClick={startCreate} disabled={saving}>
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
                                                        <td>{formatDate(p.inactiveFrom)}</td>
                                                        <td>{formatDate(p.inactiveTo)}</td>
                                                        <td>{p.inactivityReason || <span className="text-muted">neuveden</span>}</td>
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
                        <button type="button" className="btn btn-secondary" onClick={onClose} disabled={saving}>
                            Zavřít
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminPlayerInactivityModal;
