// src/components/admin/AdminPlayerEditModal.jsx
import { useEffect, useState } from "react";
import PlayerProfileForm from "../settings/PlayerProfileForm";
import { validatePlayerProfile } from "../../validation/playerValidation";

const AdminPlayerEditModal = ({ player, show, onClose, onSave, saving }) => {
    if (!show || !player) {
        return null;
    }

    const [values, setValues] = useState({
        id: player.id ?? null,
        name: player.name || "",
        surname: player.surname || "",
        nickname: player.nickname || "",
        phoneNumber: player.phoneNumber || "",
        team: player.team || "",
        type: player.type || "",
    });

    const [errors, setErrors] = useState({});

    // když se změní player, načti hodnoty znovu
    useEffect(() => {
        if (player) {
            setValues({
                id: player.id ?? null,
                name: player.name || "",
                surname: player.surname || "",
                nickname: player.nickname || "",
                phoneNumber: player.phoneNumber || "",
                team: player.team || "",
                type: player.type || "",
            });
            setErrors({});
        }
    }, [player]);

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

        const validationErrors = validatePlayerProfile(values);
        setErrors(validationErrors);

        if (Object.keys(validationErrors).length > 0) {
            return;
        }

        const payload = {
            id: values.id,
            name: values.name?.trim(),
            surname: values.surname?.trim(),
            nickname: values.nickname?.trim() || null,
            phoneNumber:
                values.phoneNumber && values.phoneNumber.trim() !== ""
                    ? values.phoneNumber.trim()
                    : null,
            team: values.team || null,
            type: values.type || null,
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
                                <h5 className="modal-title">
                                    Upravit hráče #{player.id}
                                </h5>
                                <button
                                    type="button"
                                    className="btn-close"
                                    aria-label="Close"
                                    onClick={handleClose}
                                    disabled={saving}
                                />
                            </div>

                            <div className="modal-body">
                                <PlayerProfileForm
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
                                        ? "Ukládám změny…"
                                        : "Uložit změny hráče"}
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

export default AdminPlayerEditModal;