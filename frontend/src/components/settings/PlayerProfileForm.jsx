import RoleGuard from "../RoleGuard";
import { PLAYER_POSITION_OPTIONS } from "../../constants/playerPosition";

/**
 * PlayerProfileForm
 *
 * React komponenta používaná ve frontend aplikaci.
 *
 * Props:
 * @param {Object} props.values Aktuální hodnoty formuláře.
 * @param {Function} props.onChange callback pro předání akce do nadřazené vrstvy.
 * @param {Object} props.errors Validační chyby formuláře po jednotlivých polích.
 * @param {Object} props.playerSettings data hráče nebo identifikátor aktuálního hráče.
 */
const PlayerProfileForm = ({
    values,
    onChange,
    errors = {},
    playerSettings,
}) => {
    
    const handleInputChange = (e) => {
        const { name, type, value, checked } = e.target;
        onChange({
            [name]: type === "checkbox" ? checked : value,
        });
    };

    const nameClass = "form-control" + (errors.name ? " is-invalid" : "");
    const surnameClass =
        "form-control" + (errors.surname ? " is-invalid" : "");
    const phoneClass =
        "form-control" + (errors.phoneNumber ? " is-invalid" : "");


    const canMoveTeam = !!playerSettings?.possibleMoveToAnotherTeam;
    const canChangePosition = !!playerSettings?.possibleChangePlayerPosition;

    return (
        <div>
            <h2 className="h5 mb-3">Profil hráče</h2>

            {/* JMÉNO */}
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

            {/* PŘEZDÍVKA + TELEFON */}
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

            {/* TEAM */}
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

            {/* POST */}
            <div className="row">
                <div className="col-md-6 mb-3">
                    <label className="form-label">Post</label>
                    <div className="row g-2">
                        <div className="col-12 col-sm-6">
                            <select
                                id="primaryPosition"
                                name="primaryPosition"
                                className="form-select"
                                value={values.primaryPosition || ""}
                                onChange={handleInputChange}
                            >
                                <option value="">
                                    — Primární post —
                                </option>
                                {PLAYER_POSITION_OPTIONS.map((opt) => (
                                    <option
                                        key={opt.value}
                                        value={opt.value}
                                    >
                                        {opt.label}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="col-12 col-sm-6">
                            <select
                                id="secondaryPosition"
                                name="secondaryPosition"
                                className="form-select"
                                value={values.secondaryPosition || ""}
                                onChange={handleInputChange}
                            >
                                <option value="">
                                    — Sekundární post —
                                </option>
                                {PLAYER_POSITION_OPTIONS.map((opt) => (
                                    <option
                                        key={opt.value}
                                        value={opt.value}
                                    >
                                        {opt.label}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>
                </div>
            </div>

            {/* Herní preference – zobrazení pomocí ikon z playerSettings */}
            <div className="row">
                <div className="col-md-6 mb-3">
                    <label className="form-label">
                        Herní preference (automatické přesuny z náhradníka)
                    </label>

                    {playerSettings ? (
                        <>
                            <div className="mb-2 d-flex align-items-center">
                                <span className="me-2 fs-5">
                                    {canMoveTeam ? "✅" : "❌"}
                                </span>
                                <span>
                                    {canMoveTeam
                                        ? "Můžeš být automaticky přesunut do druhého týmu při uvolnění místa."
                                        : "Nemůžeš být automaticky přesunut do druhého týmu při uvolnění místa."}
                                </span>
                            </div>

                            <div className="d-flex align-items-center">
                                <span className="me-2 fs-5">
                                    {canChangePosition ? "✅" : "❌"}
                                </span>
                                <span>
                                    {canChangePosition
                                        ? "Můžeš mít automaticky změněný post mezi obranou a útokem."
                                        : "Nemůžeš mít automaticky změněný post mezi obranou a útokem."}
                                </span>
                            </div>

                            <div className="form-text mt-2">
                                Nastavení se mění v sekci „Nastavení
                                notifikací“.
                            </div>
                        </>
                    ) : (
                        <div className="form-text">
                            Herní preference zatím nejsou načteny.
                        </div>
                    )}
                </div>
            </div>

            {/* TYP – pouze pro admin/manager */}
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
                            Typ hráče – pro zobrazení nadcházejících zápasů
                            (odpovídá enumu Typ).
                        </div>
                    </div>
                </div>
            </RoleGuard>
        </div>
    );
};

export default PlayerProfileForm;