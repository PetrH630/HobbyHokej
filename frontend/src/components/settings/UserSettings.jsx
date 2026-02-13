// src/components/settings/UserSettings.jsx
import RoleGuard from "../RoleGuard";
import { useBootstrapTooltip } from "../../hooks/useBootstrapTooltip";

const UserSettings = ({ values, onChange, onSubmit, saving, error, success }) => {
    //  inicializace tooltipů (Bootstrap)
    useBootstrapTooltip();

    const safeValues = values || {};

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

    const InfoTip = ({ text }) => (
        <span
            className="ms-2 text-muted"
            role="button"
            tabIndex={0}
            data-bs-toggle="tooltip"
            data-bs-placement="right"
            title={text}
            style={{ cursor: "help" }}
        >
            ℹ
        </span>
    );

    return (
        <form onSubmit={handleSubmit} noValidate>
            {error && <div className="alert alert-danger">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}

            {/* === Výběr hráče po přihlášení === */}
            <h2 className="h5 mb-3">Výběr hráče po přihlášení</h2>

            <div className="mb-3">
                <label className="form-label" htmlFor="playerSelectionMode">
                    Způsob výběru hráče
                    <InfoTip text="Určuje, jak se po přihlášení automaticky vybere hráč. Vhodné, pokud má uživatel pod účtem více hráčů." />
                </label>

                <select
                    id="playerSelectionMode"
                    name="playerSelectionMode"
                    className="form-select"
                    value={safeValues.playerSelectionMode || ""}
                    onChange={handleInputChange}
                    disabled={saving}
                >
                    <option
                        value="FIRST_PLAYER"
                        title="Automaticky vybere prvního hráče přiřazeného k uživateli."
                    >
                        Vybrat prvního hráče
                    </option>
                    <option
                        value="ALWAYS_CHOOSE"
                        title="Po přihlášení se vždy zobrazí dialog pro výběr hráče."
                    >
                        Vždy zobrazit dialog s výběrem hráče
                    </option>
                    <option
                        value="LAST_USED"
                        title="Po přihlášení se zvolí hráč, který byl použit naposledy."
                    >
                        Použít naposledy použitého hráče
                    </option>
                </select>

                <div className="form-text">
                    Určuje, jak se má po přihlášení vybrat hráč v kontextu
                    aplikace.
                </div>
            </div>

            <hr />

            {/* === Globální notifikace === */}
            <h2 className="h5 mb-3">Globální notifikace</h2>

            <div className="mb-3">
                <label className="form-label" htmlFor="globalNotificationLevel">
                    Úroveň notifikací
                    <InfoTip text="ALL = všechny e-maily. IMPORTANT_ONLY = jen důležité (např. zrušení zápasu). NONE = žádné notifikace." />
                </label>

                <select
                    id="globalNotificationLevel"
                    name="globalNotificationLevel"
                    className="form-select"
                    value={safeValues.globalNotificationLevel || ""}
                    onChange={handleInputChange}
                    disabled={saving}
                >
                    <option value="ALL">Všechny notifikace</option>
                    <option value="IMPORTANT_ONLY">Pouze důležité</option>
                    <option value="NONE">Žádné</option>
                </select>
            </div>

            <div className="form-check">
                <input
                    className="form-check-input"
                    type="checkbox"
                    id="copyAllPlayerNotificationsToUserEmail"
                    name="copyAllPlayerNotificationsToUserEmail"
                    checked={safeValues.copyAllPlayerNotificationsToUserEmail || false}
                    onChange={handleCheckboxChange}
                    disabled={saving}
                />
                <label
                    className="form-check-label"
                    htmlFor="copyAllPlayerNotificationsToUserEmail"
                    data-bs-toggle="tooltip"
                    data-bs-placement="right"
                    title="Pokud spravujete více hráčů, budou vám chodit kopie všech jejich notifikací."
                    style={{ cursor: "help" }}
                >
                    Posílat kopie všech notifikací hráčů na můj e-mail
                </label>
            </div>

            <div className="form-check mb-3">
                <input
                    className="form-check-input"
                    type="checkbox"
                    id="receiveNotificationsForPlayersWithOwnEmail"
                    name="receiveNotificationsForPlayersWithOwnEmail"
                    checked={safeValues.receiveNotificationsForPlayersWithOwnEmail || false}
                    onChange={handleCheckboxChange}
                    disabled={saving}
                />
                <label
                    className="form-check-label"
                    htmlFor="receiveNotificationsForPlayersWithOwnEmail"
                    data-bs-toggle="tooltip"
                    data-bs-placement="right"
                    title="I když má hráč vlastní e-mail, budete dostávat kopii notifikace také vy."
                    style={{ cursor: "help" }}
                >
                    Posílat notifikace i za hráče, kteří mají vlastní e-mail
                </label>
            </div>

            {/* === Notifikace pro manažera === */}
            <RoleGuard roles={["ROLE_MANAGER"]}>
                <div className="border rounded p-3 mb-3">
                    <h3 className="h5 mb-3">Notifikace pro manažera</h3>

                    <div className="mb-3">
                        <label className="form-label" htmlFor="managerNotificationLevel">
                            Úroveň manažerských kopií
                            <InfoTip text="Platí pouze pro kopie notifikací posílané manažerům. Neovlivňuje vaše osobní notifikace ani notifikace hráčům." />
                        </label>

                        <select
                            id="managerNotificationLevel"
                            name="managerNotificationLevel"
                            className="form-select"
                            value={
                                safeValues.managerNotificationLevel ||
                                safeValues.globalNotificationLevel ||
                                "ALL"
                            }
                            onChange={handleInputChange}
                            disabled={saving}
                        >
                            <option value="ALL">Všechny manažerské kopie</option>
                            <option value="IMPORTANT_ONLY">Jen důležité manažerské kopie</option>
                            <option value="NONE">Manažerské kopie neposílat</option>
                        </select>

                        <div className="form-text">
                            Toto nastavení se používá pouze pro kopie
                            notifikací, které jsou posílány manažerům. Nemá
                            vliv na vaše vlastní notifikace ani na notifikace
                            hráčům.
                        </div>
                    </div>
                </div>
            </RoleGuard>

            <div className="form-check mb-2">
                <input
                    className="form-check-input"
                    type="checkbox"
                    id="emailDigestEnabled"
                    name="emailDigestEnabled"
                    checked={safeValues.emailDigestEnabled || false}
                    onChange={handleCheckboxChange}
                    disabled={saving}
                />
                <label
                    className="form-check-label"
                    htmlFor="emailDigestEnabled"
                    data-bs-toggle="tooltip"
                    data-bs-placement="right"
                    title="Denní souhrn notifikací místo jednotlivých e-mailů."
                    style={{ cursor: "help" }}
                >
                    Aktivovat denní souhrn e-mailů (digest)
                </label>
            </div>

            <div className="mb-3">
                <label className="form-label" htmlFor="emailDigestTime">
                    Čas odeslání souhrnného e-mailu
                    <InfoTip text="Souhrnný e-mail bude odeslán každý den v zadaný čas. Pole je aktivní pouze při zapnutém digestu." />
                </label>

                <input
                    type="time"
                    id="emailDigestTime"
                    name="emailDigestTime"
                    className="form-control"
                    value={safeValues.emailDigestTime || ""}
                    onChange={handleInputChange}
                    disabled={!safeValues.emailDigestEnabled || saving}
                />
            </div>

            <hr />

            {/* === Uživatelské rozhraní === */}
            <h2 className="h5 mb-3">Uživatelské rozhraní</h2>

            <div className="mb-3">
                <label className="form-label" htmlFor="uiLanguage">
                    Jazyk rozhraní
                    <InfoTip text="Volba jazyka uživatelského rozhraní aplikace." />
                </label>

                <select
                    id="uiLanguage"
                    name="uiLanguage"
                    className="form-select"
                    value={safeValues.uiLanguage || ""}
                    onChange={handleInputChange}
                    disabled={saving}
                >
                    <option value="cs">Čeština</option>
                    <option value="en">Angličtina</option>
                </select>
            </div>

            <div className="mb-3">
                <label className="form-label" htmlFor="timezone">
                    Časová zóna
                    <InfoTip text="Používá se pro zobrazení časů v aplikaci a plánování notifikací. Doporučeno: Europe/Prague." />
                </label>

                <input
                    type="text"
                    id="timezone"
                    name="timezone"
                    className="form-control"
                    value={safeValues.timezone || ""}
                    onChange={handleInputChange}
                    placeholder="např. Europe/Prague"
                    disabled={saving}
                />
            </div>

            <div className="mb-3">
                <label className="form-label" htmlFor="defaultLandingPage">
                    Výchozí obrazovka po přihlášení
                    <InfoTip text="Určuje, na kterou stránku budete po přihlášení přesměrován." />
                </label>

                <select
                    id="defaultLandingPage"
                    name="defaultLandingPage"
                    className="form-select"
                    value={safeValues.defaultLandingPage || ""}
                    onChange={handleInputChange}
                    disabled={saving}
                >
                    <option value="MATCHES">Zápasy</option>
                    <option value="PLAYERS">Hráči</option>
                    <option value="DASHBOARD">Přehled</option>
                </select>
            </div>

            <div className="mt-4 d-flex justify-content-end">
                <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={saving}
                >
                    {saving ? "Ukládám…" : "Uložit nastavení uživatele"}
                </button>
            </div>
        </form>
    );
};

export default UserSettings;
