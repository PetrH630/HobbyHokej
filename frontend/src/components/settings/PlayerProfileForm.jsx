// src/components/settings/PlayerProfileForm.jsx
import RoleGuard from "../RoleGuard";

const PlayerProfileForm = ({ values, onChange, errors = {} }) => {
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        onChange({ [name]: value });
    };

    const nameClass =
        "form-control" + (errors.name ? " is-invalid" : "");
    const surnameClass =
        "form-control" + (errors.surname ? " is-invalid" : "");
    const phoneClass =
        "form-control" + (errors.phoneNumber ? " is-invalid" : "");

    return (
        <div>
            <h2 className="h5 mb-3">Profil hráče</h2>

            <div className="row">
                <div className="col-md-6 mb-3">
                    <label className="form-label" htmlFor="name">
                        Křestní jméno
                    </label>
                    <input
                        type="text"
                        id="name"
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
                    <label className="form-label" htmlFor="surname">
                        Příjmení
                    </label>
                    <input
                        type="text"
                        id="surname"
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

            <div className="row">
                <div className="col-md-6 mb-3">
                    <label className="form-label" htmlFor="nickname">
                        Přezdívka
                    </label>
                    <input
                        type="text"
                        id="nickname"
                        name="nickname"
                        className="form-control"
                        value={values.nickname || ""}
                        onChange={handleInputChange}
                    />
                </div>

                <div className="col-md-6 mb-3">
                    <label className="form-label" htmlFor="phoneNumber">
                        Telefon
                    </label>
                    <input
                        type="tel"
                        id="phoneNumber"
                        name="phoneNumber"
                        className={phoneClass}
                        placeholder="+420123456789"
                        value={values.phoneNumber || ""}
                        onChange={handleInputChange}
                    />
                    {errors.phoneNumber && (
                        <div className="invalid-feedback">
                            {errors.phoneNumber}
                        </div>
                    )}
                    {!errors.phoneNumber && (
                        <div className="form-text">
                            Telefon zadej v mezinárodním formátu, např.{" "}
                            <strong>+420123456789</strong>.
                        </div>
                    )}
                </div>
            </div>

            {/* TEAM – výběr týmu hráče */}
            <div className="row">
                <div className="col-md-6 mb-3">
                    <label className="form-label" htmlFor="team">
                        Tým
                    </label>
                    <select
                        id="team"
                        name="team"
                        className="form-select"
                        value={values.team || ""}
                        onChange={handleInputChange}
                    >
                        <option value="">— Není přiřazen —</option>
                        <option value="LIGHT">Světlý tým</option>
                        <option value="DARK">Tmavý tým</option>
                    </select>
                    <div className="form-text">
                        Tým, ke kterému je hráč přiřazen (odpovídá enumu Team).
                    </div>
                </div>
            </div>
            {/* TYP – výběr typu hráče */}
            <RoleGuard roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
            <div className="row">
                <div className="col-md-6 mb-3">
                    <label className="form-label" htmlFor="type">
                        Typ
                    </label>
                    <select
                        id="type"
                        name="type"
                        className="form-select"
                        value={values.type || ""}
                        onChange={handleInputChange}
                    >
                        <option value="BASIC">základní</option>
                        <option value="STANDARD">standardní</option>
                        <option value="VIP">VIP</option>
                    </select>
                    <div className="form-text">
                        Typ hráče - pro zobrazení nadcházejících zápasů (odpovídá enumu Typ).
                    </div>
                </div>
              </div>
            </RoleGuard> 
        </div>
    );
};

export default PlayerProfileForm;
