// src/components/admin/AdminUserEditModal.jsx
import { useEffect, useState } from "react";
import { useGlobalModal } from "../../hooks/useGlobalModal";


const AdminUserEditModal = ({ user, show, onClose, onSave, saving }) => {
  

    if (!show || !user) {
        return null;
    }

    const [values, setValues] = useState({
        id: user.id ?? null,
        name: user.name || "",
        surname: user.surname || "",
        email: user.email || "",
    });

    const [errors, setErrors] = useState({});

    useEffect(() => {
        if (user) {
            setValues({
                id: user.id ?? null,
                name: user.name || "",
                surname: user.surname || "",
                email: user.email || "",
            });
            setErrors({});
        }
    }, [user]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setValues((prev) => ({ ...prev, [name]: value }));

        setErrors((prev) => {
            const copy = { ...prev };
            delete copy[name];
            return copy;
        });
    };

    const validate = () => {
        const errs = {};

        if (!values.name || !values.name.trim()) {
            errs.name = "Jméno je povinné.";
        }
        if (!values.surname || !values.surname.trim()) {
            errs.surname = "Příjmení je povinné.";
        }
        if (!values.email || !values.email.trim()) {
            errs.email = "E-mail je povinný.";
        } else if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(values.email.trim())) {
            errs.email = "E-mail nemá platný formát.";
        }

        return errs;
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        const validationErrors = validate();
        setErrors(validationErrors);

        if (Object.keys(validationErrors).length > 0) {
            return;
        }

        const payload = {
            id: values.id,
            name: values.name.trim(),
            surname: values.surname.trim(),
            email: values.email.trim(),
        };

        onSave(payload);
    };

    const handleClose = () => {
        if (!saving) {
            onClose();
        }
    };

    const nameClass =
        "form-control" + (errors.name ? " is-invalid" : "");
    const surnameClass =
        "form-control" + (errors.surname ? " is-invalid" : "");
    const emailClass =
        "form-control" + (errors.email ? " is-invalid" : "");

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
                                    Upravit uživatele #{user.id}
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
                                <div className="mb-3">
                                    <label
                                        className="form-label"
                                        htmlFor="name"
                                    >
                                        Jméno
                                    </label>
                                    <input
                                        type="text"
                                        id="name"
                                        name="name"
                                        className={nameClass}
                                        value={values.name}
                                        onChange={handleChange}
                                    />
                                    {errors.name && (
                                        <div className="invalid-feedback">
                                            {errors.name}
                                        </div>
                                    )}
                                </div>

                                <div className="mb-3">
                                    <label
                                        className="form-label"
                                        htmlFor="surname"
                                    >
                                        Příjmení
                                    </label>
                                    <input
                                        type="text"
                                        id="surname"
                                        name="surname"
                                        className={surnameClass}
                                        value={values.surname}
                                        onChange={handleChange}
                                    />
                                    {errors.surname && (
                                        <div className="invalid-feedback">
                                            {errors.surname}
                                        </div>
                                    )}
                                </div>

                                <div className="mb-3">
                                    <label
                                        className="form-label"
                                        htmlFor="email"
                                    >
                                        E-mail
                                    </label>
                                    <input
                                        type="email"
                                        id="email"
                                        name="email"
                                        className={emailClass}
                                        value={values.email}
                                        onChange={handleChange}
                                    />
                                    {errors.email && (
                                        <div className="invalid-feedback">
                                            {errors.email}
                                        </div>
                                    )}
                                </div>
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
                                        : "Uložit změny uživatele"}
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

export default AdminUserEditModal;
