// src/components/admin/AdminSeasonModal.jsx
import { useEffect, useState } from "react";
import SeasonForm from "../seasons/SeasonForm";
import { validateSeason } from "../../validation/seasonValidation";
import { useGlobalModal } from "../../hooks/useGlobalModal";


/**
 * AdminSeasonModal
 *
 * Bootstrap modal komponenta pro práci s modálním dialogem v aplikaci.
 *
 * Umožňuje zavření stiskem klávesy Escape.
 * Při otevření blokuje scroll pozadí pomocí useGlobalModal.
 *
 * Props:
 * @param {import("../../types/dto").SeasonDTO} props.season Data sezóny používaná ve správě sezón.
 * @param {boolean} props.show určuje, zda je dialog otevřený.
 * @param {Function} props.onClose callback pro předání akce do nadřazené vrstvy.
 * @param {Function} props.onSave vstupní hodnota komponenty.
 * @param {boolean} props.saving Příznak, že probíhá ukládání a akce mají být dočasně blokovány.
 * @param {Object} props.allSeasons vstupní hodnota komponenty. [default: []]
 * @param {string} props.serverError Chybová zpráva vrácená ze serveru.
 */

const AdminSeasonModal = ({
    season,
    show,
    onClose,
    onSave,
    saving,
    allSeasons = [],
    serverError, 
}) => {
   
    if (!show) {
        return null;
    }

    const [values, setValues] = useState({
        id: season?.id ?? null,
        name: season?.name || "",
        startDate: season?.startDate || "",
        endDate: season?.endDate || "",
        active: season?.active ?? false, 
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
                active: season.active ?? false, 
            });
        } else {
            setValues({
                id: null,
                name: "",
                startDate: "",
                endDate: "",
                active: false, 
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

    
/**
 * Zpracuje odeslání formuláře a zavolá příslušný callback nadřazené komponenty.
 */

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
            active: values.active, 
        };

        onSave(payload);
    };

    
/**
 * Zajistí konzistentní zavření modalu a vrácení lokálního stavu do výchozího nastavení.
 */

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
