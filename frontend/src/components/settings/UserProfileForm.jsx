// src/components/settings/UserProfileForm.jsx

const UserProfileForm = ({ values, onChange, errors = {} }) => {
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        onChange({ [name]: value });
    };

    const nameClass =
        "form-control" + (errors.name ? " is-invalid" : "");
    const surnameClass =
        "form-control" + (errors.surname ? " is-invalid" : "");
    const emailClass =
        "form-control" + (errors.email ? " is-invalid" : "");

    const roleLabel = values?.role ?? "";
    const enabledLabel =
        typeof values?.enabled === "boolean"
            ? values.enabled
                ? "Aktivní"
                : "Neaktivní"
            : "";

    return (
        <div>
            <h2 className="h5 mb-3">Profil uživatele</h2>

            {/* řádek: jméno / příjmení */}
            <div className="row">
                <div className="col-md-6 mb-3">
                    <label className="form-label" htmlFor="user-name">
                        Křestní jméno
                    </label>
                    <input
                        type="text"
                        id="user-name"
                        name="name"
                        className={nameClass}
                        value={values.name || ""}
                        onChange={handleInputChange}
                    />
                    {errors.name && (
                        <div className="invalid-feedback">
                            {errors.name}
                        </div>
                    )}
                </div>

                <div className="col-md-6 mb-3">
                    <label className="form-label" htmlFor="user-surname">
                        Příjmení
                    </label>
                    <input
                        type="text"
                        id="user-surname"
                        name="surname"
                        className={surnameClass}
                        value={values.surname || ""}
                        onChange={handleInputChange}
                    />
                    {errors.surname && (
                        <div className="invalid-feedback">
                            {errors.surname}
                        </div>
                    )}
                </div>
            </div>

            {/* řádek: e-mail / role + stav účtu */}
            <div className="row">
                <div className="col-md-6 mb-3">
                    <label className="form-label" htmlFor="user-email">
                        E-mail (přihlašovací)
                    </label>
                    <input
                        type="email"
                        id="user-email"
                        name="email"
                        className={emailClass}
                        value={values.email || ""}
                        onChange={handleInputChange}
                        disabled
                        readOnly
                    />
                    {errors.email && (
                        <div className="invalid-feedback">
                            {errors.email}
                        </div>
                    )}
                    {!errors.email && (
                        <div className="form-text">
                            E-mail slouží jako přihlašovací jméno a v tomto
                            formuláři jej nelze měnit.
                        </div>
                    )}
                </div>

                <div className="col-md-6 mb-3">
                    <label className="form-label">
                        Role v systému / stav účtu
                    </label>
                    <div className="d-flex flex-column gap-2">
                        <input
                            type="text"
                            className="form-control"
                            value={roleLabel}
                            readOnly
                            disabled
                        />
                        <input
                            type="text"
                            className="form-control"
                            value={enabledLabel}
                            readOnly
                            disabled
                        />
                    </div>
                    <div className="form-text">
                        Změnu role nebo aktivace/deaktivace účtu provádí
                        administrátor.
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UserProfileForm;
