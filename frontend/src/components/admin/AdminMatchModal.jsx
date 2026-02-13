// src/components/admin/AdminMatchModal.jsx
import { useEffect, useState } from "react";
import MatchForm from "../matches/MatchForm";
import { validateMatch } from "../../validation/matchValidation";
import { tryClearDemoNotifications } from "../../api/demoApi";
import { useGlobalModal } from "../../hooks/useGlobalModal";


// BE "yyyy-MM-dd HH:mm:ss" nebo ISO → "yyyy-MM-ddTHH:mm" pro <input type="datetime-local">
const toInputDateTime = (value) => {
    if (!value) return "";

    // Pokud už je to ve formátu s T (ISO 8601), jen uřízneme na minuty
    if (value.includes("T")) {
        return value.slice(0, 16); // yyyy-MM-ddTHH:mm
    }

    // Očekávaný formát "yyyy-MM-dd HH:mm:ss"
    const [date, time] = value.split(" ");
    if (!time) return value;

    const [hh = "00", mm = "00"] = time.split(":");
    return `${date}T${hh}:${mm}`;
};

const AdminMatchModal = ({
    match,
    show,
    onClose,
    onSave,
    saving,
    serverError,
}) => {
    

    if (!show) {
        return null;
    }

    const [values, setValues] = useState({
        id: match?.id ?? null,
        dateTime: match?.dateTime ? toInputDateTime(match.dateTime) : "",
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
    });

    const [errors, setErrors] = useState({});

    const isNew = !values.id;

    useEffect(() => {
        if (match) {
            setValues({
                id: match.id ?? null,
                dateTime: match.dateTime ? toInputDateTime(match.dateTime) : "",
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

        if (Object.keys(validationErrors).length > 0) {
            return;
        }

        // DEMO: před operací vyčistit staré notifikace (pojistka proti "lepení")
        // V produkci endpoint neexistuje → tryClearDemoNotifications to bezpečně ignoruje.
        await tryClearDemoNotifications();

        // Převod dateTime z "yyyy-MM-ddTHH:mm" na "yyyy-MM-dd HH:mm:ss"
        const toBackendDateTime = (value) => {
            if (!value) return null;
            if (value.includes("T")) {
                const [date, time] = value.split("T");
                const [hh = "00", mm = "00"] = time.split(":");
                return `${date} ${hh}:${mm}:00`;
            }
            return value;
        };

        const payload = {
            id: values.id,
            dateTime: toBackendDateTime(values.dateTime),
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
                values.price === "" || values.price === null
                    ? null
                    : Number(values.price),
        };

        onSave(payload);
    };


    const handleClose = () => {
        if (!saving) {
            onClose();
        }
    };

    return (
        <>
            <div
                className="modal fade show d-block"
                tabIndex="-1"
                role="dialog"
                aria-modal="true"
            >
                <div className="modal-dialog modal-lg" role="document">
                    <div className="modal-content">
                        <form onSubmit={handleSubmit} noValidate>
                            <div className="modal-header">
                                <div>
                                    <h5 className="modal-title">
                                        {isNew
                                            ? "Vytvořit nový zápas"
                                            : `Upravit zápas #${values.id}${values.matchNumber
                                                ? ` (č. ${values.matchNumber} v sezóně)`
                                                : ""
                                            }`}
                                    </h5>
                                    {!isNew && values.seasonId && (
                                        <small className="text-muted">
                                            Sezóna ID: {values.seasonId}
                                        </small>
                                    )}
                                </div>
                                <button
                                    type="button"
                                    className="btn-close"
                                    aria-label="Close"
                                    onClick={handleClose}
                                    disabled={saving}
                                ></button>
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

                                {!isNew && (
                                    <div className="mt-3">
                                        <small className="text-muted">
                                            Stav zápasu:{" "}
                                            {values.matchStatus || "NENASTAVEN"}
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
                                <button
                                    type="submit"
                                    className="btn btn-primary"
                                    disabled={saving}
                                >
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
