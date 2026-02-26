// src/components/settings/PlayerSettings.jsx
const PlayerSettings = ({
    values,
    onChange,
    onSubmit,
    saving,
    error,
    success,
    errors = {}, // 🔹 field-level chyby z SettingsPage
}) => {
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        onChange({ [name]: value });
    };

    const handleCheckboxChange = (e) => {
        const { name, checked } = e.target;
        onChange({ [name]: checked });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit();
    };

    return (
        <form onSubmit={handleSubmit}>
            {error && <div className="alert alert-danger">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}

            <h2 className="mb-3">Nastavení notifikací</h2>
            <h2 className="h5 mb-3">Kontaktní údaje</h2>

            {/* Kontaktní e-mail */}
            <div className="mb-3">
                <label className="form-label" htmlFor="contactEmail">
                    Kontaktní e-mail - pokud chceš používat tento, dej jen "uložit nastavení notifikaci" 
                </label>
                <input
                    type="email"
                    id="contactEmail"
                    name="contactEmail"
                    className={
                        "form-control" +
                        (errors.contactEmail ? " is-invalid" : "")
                    }
                    value={values.contactEmail || ""}
                    onChange={handleInputChange}
                />
                {errors.contactEmail && (
                    <div className="invalid-feedback">
                        {errors.contactEmail}
                    </div>
                )}
                <div className="form-text">
                    Na tento e-mail mohou chodit notifikační zprávy.
                </div>
            </div>

            {/* Kontaktní telefon */}
            <div className="mb-3">
                <label className="form-label" htmlFor="contactPhone">
                    Kontaktní telefon
                </label>
                <input
                    type="tel"
                    id="contactPhone"
                    name="contactPhone"
                    className={
                        "form-control" +
                        (errors.contactPhone ? " is-invalid" : "")
                    }
                    value={values.contactPhone || ""}
                    onChange={handleInputChange}
                />
                {errors.contactPhone && (
                    <div className="invalid-feedback">
                        {errors.contactPhone}
                    </div>
                )}
                <div className="form-text">
                    Telefonní číslo pro SMS notifikace.
                </div>
            </div>

            <hr />

            <h2 className="h5 mb-3">Kanály</h2>

            <div className="form-check mb-2">
                <input
                    className="form-check-input"
                    type="checkbox"
                    id="emailEnabled"
                    name="emailEnabled"
                    checked={values.emailEnabled || false}
                    onChange={handleCheckboxChange}
                />
                <label className="form-check-label" htmlFor="emailEnabled">
                    Povolit e-mailové notifikace
                </label>
            </div>

            <div className="form-check mb-3">
                <input
                    className="form-check-input"
                    type="checkbox"
                    id="smsEnabled"
                    name="smsEnabled"
                    checked={values.smsEnabled || false}
                    onChange={handleCheckboxChange}
                />
                <label className="form-check-label" htmlFor="smsEnabled">
                    Povolit SMS notifikace
                </label>
            </div>

            <hr />

            <h2 className="h5 mb-3">Události</h2>

            <div className="form-check">
                <input
                    className="form-check-input"
                    type="checkbox"
                    id="notifyOnRegistration"
                    name="notifyOnRegistration"
                    checked={values.notifyOnRegistration || false}
                    onChange={handleCheckboxChange}
                />
                <label
                    className="form-check-label"
                    htmlFor="notifyOnRegistration"
                >
                    Informovat při přihlášení / odhlášení na zápas
                </label>
            </div>

            <div className="form-check">
                <input
                    className="form-check-input"
                    type="checkbox"
                    id="notifyOnExcuse"
                    name="notifyOnExcuse"
                    checked={values.notifyOnExcuse || false}
                    onChange={handleCheckboxChange}
                />
                <label className="form-check-label" htmlFor="notifyOnExcuse">
                    Informovat o omluvě
                </label>
            </div>

            <div className="form-check">
                <input
                    className="form-check-input"
                    type="checkbox"
                    id="notifyOnMatchChange"
                    name="notifyOnMatchChange"
                    checked={values.notifyOnMatchChange || false}
                    onChange={handleCheckboxChange}
                />
                <label
                    className="form-check-label"
                    htmlFor="notifyOnMatchChange"
                >
                    Informovat při změně zápasu (čas, místo…)
                </label>
            </div>

            <div className="form-check">
                <input
                    className="form-check-input"
                    type="checkbox"
                    id="notifyOnMatchCancel"
                    name="notifyOnMatchCancel"
                    checked={values.notifyOnMatchCancel || false}
                    onChange={handleCheckboxChange}
                />
                <label
                    className="form-check-label"
                    htmlFor="notifyOnMatchCancel"
                >
                    Informovat při zrušení zápasu
                </label>
            </div>
          

            <hr />

            <h2 className="h5 mb-3">Připomínky zápasů</h2>

            <div className="form-check mb-2">
                <input
                    className="form-check-input"
                    type="checkbox"
                    id="notifyReminders"
                    name="notifyReminders"
                    checked={values.notifyReminders || false}
                    onChange={handleCheckboxChange}
                />
                <label className="form-check-label" htmlFor="notifyReminders">
                    Posílat připomínky před zápasem
                </label>
            </div>

            <div className="mb-3">
                <label
                    className="form-label"
                    htmlFor="reminderHoursBefore"
                >
                    Kolik hodin před zápasem poslat připomínku
                </label>
                <input
                    type="number"
                    id="reminderHoursBefore"
                    name="reminderHoursBefore"
                    className="form-control"
                    min={1}
                    max={72}
                    value={values.reminderHoursBefore ?? ""}
                    onChange={(e) =>
                        onChange({
                            reminderHoursBefore: e.target.value
                                ? Number(e.target.value)
                                : null,
                        })
                    }
                    disabled={!values.notifyReminders}
                />
            </div>
            <div className="d-flex justify-content-end">         
            <div className="mt-4">
                <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={saving}
                >
                    {saving ? "Ukládám…" : "Uložit nastavení notifikaci"}
                </button>
                </div>
            </div>
        </form>
    );
};

export default PlayerSettings;
