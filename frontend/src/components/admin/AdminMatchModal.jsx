// src/components/admin/AdminMatchModal.jsx
import { useEffect, useState } from "react";
import MatchForm from "../matches/MatchForm";
import { validateMatch } from "../../validation/matchValidation";
import { tryClearDemoNotifications } from "../../api/demoApi";

// backend "yyyy-MM-dd HH:mm:ss" -> picker "YYYY-MM-DDTHH:mm"
const toPickerValue = (value) => {
    if (!value) return "";

    const s = String(value);

    // ISO
    if (s.includes("T")) {
        return s.slice(0, 16);
    }

    // backend "yyyy-MM-dd HH:mm:ss"
    return s.replace(" ", "T").slice(0, 16);
};

// picker "YYYY-MM-DDTHH:mm" -> backend "yyyy-MM-dd HH:mm:ss"
const toBackendDateTime = (valueString) => {
    if (!valueString) return null;

    // očekáváme "YYYY-MM-DDTHH:mm"
    if (String(valueString).includes("T")) {
        const [date, time] = String(valueString).split("T");
        const [hh = "00", mm = "00"] = String(time).split(":");
        return `${date} ${hh}:${mm}:00`;
    }

    // fallback
    return String(valueString);
};

const AdminMatchModal = ({ match, show, onClose, onSave, saving, serverError }) => {
    if (!show) return null;

    const [values, setValues] = useState({
        id: match?.id ?? null,
        dateTime: match?.dateTime ? toPickerValue(match.dateTime) : "", // ✅ string
        location: match?.location || "",
        description: match?.description || "",
        maxPlayers:
            match?.maxPlayers !== undefined && match?.maxPlayers !== null
                ? match.maxPlayers
                : "",
        price: match?.price !== undefined && match?.price !== null ? match.price : "",
        matchStatus: match?.matchStatus || null,
        cancelReason: match?.cancelReason || null,
        matchNumber: match?.matchNumber || null,
        seasonId: match?.seasonId || null,
    });

    const [errors, setErrors] = useState({});
    const isNew = !values.id;

    useEffect(() => {
        if (match) {
            setValues({
                id: match.id ?? null,
                dateTime: match?.dateTime ? toPickerValue(match.dateTime) : "",
                location: match.location || "",
                description: match.description || "",
                maxPlayers:
                    match.maxPlayers !== undefined && match.maxPlayers !== null
                        ? match.maxPlayers
                        : "",
                price: match.price !== undefined && match.price !== null ? match.price : "",
                matchStatus: match.matchStatus || null,
                cancelReason: match.cancelReason || null,
                matchNumber: match.matchNumber || null,
                seasonId: match.seasonId || null,
            });
        } else {
            setValues({
                id: null,
                dateTime: "",
                location: "",
                description: "",
                maxPlayers: "",
                price: "",
                matchStatus: null,
                cancelReason: null,
                matchNumber: null,
                seasonId: null,
            });
        }
        setErrors({});
    }, [match]);

    const handleChange = (patch) => {
        setValues((prev) => ({ ...prev, ...patch }));

        const key = Object.keys(patch)[0];
        setErrors((prev) => {
            const copy = { ...prev };
            delete copy[key];
            return copy;
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const validationErrors = validateMatch(values);
        setErrors(validationErrors);

        if (Object.keys(validationErrors).length > 0) return;

        await tryClearDemoNotifications();

        const payload = {
            id: values.id,
            dateTime: toBackendDateTime(values.dateTime), // ✅ string -> backend string
            location: values.location?.trim(),
            description:
                values.description && values.description.trim() !== ""
                    ? values.description.trim()
                    : null,
            maxPlayers:
                values.maxPlayers === "" || values.maxPlayers === null
                    ? null
                    : Number(values.maxPlayers),
            price:
                values.price === "" || values.price === null ? null : Number(values.price),
        };

        onSave(payload);
    };

    const handleClose = () => {
        if (!saving) onClose();
    };

    return (
        <>
            <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true">
                <div className="modal-dialog modal-lg" role="document">
                    <div className="modal-content">
                        <form onSubmit={handleSubmit} noValidate>
                            <div className="modal-header">
                                <div>
                                    <h5 className="modal-title">
                                        {isNew
                                            ? "Vytvořit nový zápas"
                                            : `Upravit zápas #${values.id}${values.matchNumber ? ` (č. ${values.matchNumber} v sezóně)` : ""
                                            }`}
                                    </h5>
                                    {!isNew && values.seasonId && (
                                        <small className="text-muted">Sezóna ID: {values.seasonId}</small>
                                    )}
                                </div>

                                <button
                                    type="button"
                                    className="btn-close"
                                    aria-label="Close"
                                    onClick={handleClose}
                                    disabled={saving}
                                />
                            </div>

                            <div className="modal-body">
                                {serverError && <div className="alert alert-danger">{serverError}</div>}

                                <MatchForm values={values} onChange={handleChange} errors={errors} />

                                {!isNew && (
                                    <div className="mt-3">
                                        <small className="text-muted">
                                            Stav zápasu: {values.matchStatus || "NENASTAVEN"}
                                            {values.cancelReason &&
                                                ` (důvod zrušení: ${values.cancelReason})`}
                                        </small>
                                    </div>
                                )}
                            </div>

                            <div className="modal-footer">
                                <button
                                    type="button"
                                    className="btn btn-secondary"
                                    onClick={handleClose}
                                    disabled={saving}
                                >
                                    Zavřít
                                </button>

                                <button type="submit" className="btn btn-primary" disabled={saving}>
                                    {saving
                                        ? isNew
                                            ? "Vytvářím zápas…"
                                            : "Ukládám změny…"
                                        : isNew
                                            ? "Vytvořit zápas"
                                            : "Uložit změny zápasu"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <div className="modal-backdrop fade show"></div>
        </>
    );
};

export default AdminMatchModal;
