// src/components/admin/AdminSeasonModal.jsx
import { useEffect, useState } from "react";
import SeasonForm from "../seasons/SeasonForm";
import { validateSeason } from "../../validation/seasonValidation";
import { useGlobalModal } from "../../hooks/useGlobalModal";


const AdminSeasonModal = ({
    season,
    show,
    onClose,
    onSave,
    saving,
    allSeasons = [],
    serverError, // text chyby z backendu (např. InvalidSeasonStateException apod.)
}) => {
   
    if (!show) {
        return null;
    }

    const [values, setValues] = useState({
        id: season?.id ?? null,
        name: season?.name || "",
        startDate: season?.startDate || "",
        endDate: season?.endDate || "",
        active: season?.active ?? false, // 👈 držíme aktivní stav
    });

    const [errors, setErrors] = useState({});

    const isNew = !values.id;

    useEffect(() => {
        if (season) {
            setValues({
                id: season.id ?? null,
                name: season.name || "",
                startDate: season.startDate || "",
                endDate: season.endDate || "",
                active: season.active ?? false, // 👈 z backendu
            });
        } else {
            setValues({
                id: null,
                name: "",
                startDate: "",
                endDate: "",
                active: false, // nová sezóna je defaultně neaktivní
            });
        }
        setErrors({});
    }, [season]);

    const handleChange = (patch) => {
        setValues((prev) => ({ ...prev, ...patch }));

        const key = Object.keys(patch)[0];
        setErrors((prev) => {
            const copy = { ...prev };
            delete copy[key];
            return copy;
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        const validationErrors = validateSeason(values, allSeasons);
        setErrors(validationErrors);

        if (Object.keys(validationErrors).length > 0) {
            return;
        }

        const payload = {
            id: values.id,
            name: values.name?.trim(),
            startDate: values.startDate,
            endDate: values.endDate,
            active: values.active, // 👈 posíláme původní stav
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
                <div className="modal-dialog" role="document">
                    <div className="modal-content">
                        <form onSubmit={handleSubmit} noValidate>
                            <div className="modal-header">
                                <h5 className="modal-title">
                                    {isNew
                                        ? "Vytvořit novou sezónu"
                                        : `Upravit sezónu #${values.id}`}
                                </h5>
                                <button
                                    type="button"
                                    className="btn-close"
                                    aria-label="Close"
                                    onClick={handleClose}
                                    disabled={saving}
                                ></button>
                            </div>

                            <div className="modal-body">
                                {/* Server-side chyba (např. InvalidSeasonStateException,
                                    DuplicateSeasonNameException atd.) */}
                                {serverError && (
                                    <div className="alert alert-danger">
                                        {serverError}
                                    </div>
                                )}

                                <SeasonForm
                                    values={values}
                                    onChange={handleChange}
                                    errors={errors}
                                />
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
                                            ? "Vytvářím sezónu…"
                                            : "Ukládám změny…"
                                        : isNew
                                            ? "Vytvořit sezónu"
                                            : "Uložit změny sezóny"}
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

export default AdminSeasonModal;
