// src/components/admin/AdminMatchModal.jsx
import { useEffect, useState } from "react";
import MatchForm from "../matches/MatchForm";
import { validateMatch } from "../../validation/matchValidation";
import { tryClearDemoNotifications } from "../../api/demoApi";
import {
    MATCH_MODE_OPTIONS,
    calculateMaxPlayers,
} from "../../constants/matchModeConfig";

// backend "yyyy-MM-dd HH:mm:ss" -> picker
const toPickerValue = (value) => {
    if (!value) return "";
    const s = String(value);
    if (s.includes("T")) return s.slice(0, 16);
    return s.replace(" ", "T").slice(0, 16);
};

// picker -> backend
const toBackendDateTime = (valueString) => {
    if (!valueString) return null;
    if (valueString.includes("T")) {
        const [date, time] = valueString.split("T");
        const [hh = "00", mm = "00"] = time.split(":");
        return `${date} ${hh}:${mm}:00`;
    }
    return valueString;
};

const AdminMatchModal = ({
    match,
    show,
    onClose,
    onSave,
    saving,
    serverError,
}) => {
    if (!show) return null;

    const isNew = !match?.id;

    const [values, setValues] = useState({
        id: match?.id ?? null,
        dateTime: match?.dateTime ? toPickerValue(match.dateTime) : "",
        location: match?.location || "",
        description: match?.description || "",
        maxPlayers:
            match?.maxPlayers !== undefined && match?.maxPlayers !== null
                ? match.maxPlayers
                : "",
        price:
            match?.price !== undefined && match?.price !== null
                ? match.price
                : "",
        matchStatus: match?.matchStatus || null,
        cancelReason: match?.cancelReason || null,
        matchNumber: match?.matchNumber || null,
        seasonId: match?.seasonId || null,
        matchMode: match?.matchMode || null,
    });

    const [errors, setErrors] = useState({});

    useEffect(() => {
        if (match) {
            setValues({
                id: match.id ?? null,
                dateTime: match?.dateTime ? toPickerValue(match.dateTime) : "",
                location: match.location || "",
                description: match.description || "",
                maxPlayers:
                    match.maxPlayers !== undefined &&
                        match.maxPlayers !== null
                        ? match.maxPlayers
                        : "",
                price:
                    match.price !== undefined && match.price !== null
                        ? match.price
                        : "",
                matchStatus: match.matchStatus || null,
                cancelReason: match.cancelReason || null,
                matchNumber: match.matchNumber || null,
                seasonId: match.seasonId || null,
                matchMode: match.matchMode || null,
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
                matchMode: null,
            });
        }
        setErrors({});
    }, [match]);

    /*
     * Automatické přepočítání maxPlayers při změně matchMode
     * pouze při vytváření nového zápasu
     */
    useEffect(() => {
        if (!isNew) return;
        if (!values.matchMode) return;

        const calculated = calculateMaxPlayers(values.matchMode);

        setValues((prev) => ({
            ...prev,
            maxPlayers: calculated,
        }));
    }, [values.matchMode, isNew]);

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
            dateTime: toBackendDateTime(values.dateTime),
            location: values.location?.trim(),
            description:
                values.description?.trim() !== ""
                    ? values.description.trim()
                    : null,
            maxPlayers:
                values.maxPlayers === "" ? null : Number(values.maxPlayers),
            price: values.price === "" ? null : Number(values.price),
            matchMode: values.matchMode || null,
        };

        onSave(payload);
    };

    const handleClose = () => {
        if (!saving) onClose();
    };

    return (
        <>
            <div className="modal fade show d-block">
                <div className="modal-dialog modal-lg">
                    <div className="modal-content">
                        <form onSubmit={handleSubmit} noValidate>
                            <div className="modal-header">
                                <h5 className="modal-title">
                                    {isNew
                                        ? "Vytvořit nový zápas"
                                        : `Upravit zápas #${values.id}`}
                                </h5>
                                <button
                                    type="button"
                                    className="btn-close"
                                    onClick={handleClose}
                                    disabled={saving}
                                />
                            </div>

                            <div className="modal-body">
                                {serverError && (
                                    <div className="alert alert-danger">
                                        {serverError}
                                    </div>
                                )}

                                <MatchForm
                                    values={values}
                                    onChange={handleChange}
                                    errors={errors}
                                />

                                {/* MATCH MODE SELECT */}
                                <div className="mb-3 mt-3">
                                    <label className="form-label">
                                        Herní systém
                                    </label>
                                    <select
                                        className="form-select"
                                        value={values.matchMode || ""}
                                        onChange={(e) =>
                                            handleChange({
                                                matchMode:
                                                    e.target.value || null,
                                            })
                                        }
                                        disabled={saving}
                                    >
                                        <option value="">
                                            -- Vyber herní systém --
                                        </option>

                                        {MATCH_MODE_OPTIONS.map((option) => (
                                            <option
                                                key={option.value}
                                                value={option.value}
                                            >
                                                {option.label}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                {!isNew && (
                                    <div className="mt-3">
                                        <small className="text-muted">
                                            Stav zápasu:{" "}
                                            {values.matchStatus ||
                                                "NENASTAVEN"}
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

                                <button
                                    type="submit"
                                    className="btn btn-primary"
                                    disabled={saving}
                                >
                                    {saving
                                        ? "Ukládám…"
                                        : isNew
                                            ? "Vytvořit zápas"
                                            : "Uložit změny"}
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