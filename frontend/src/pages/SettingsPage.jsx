// src/pages/SettingsPage.jsx
import { useEffect, useState } from "react";
import { usePlayerSettings } from "../hooks/usePlayerSettings";
import { useUserSettings } from "../hooks/useUserSettings";
import { useCurrentPlayer } from "../hooks/useCurrentPlayer";

import { updateMyCurrentPlayer } from "../api/playerApi";

import PlayerSettings from "../components/settings/PlayerSettings";
import UserSettings from "../components/settings/UserSettings";
import PlayerProfileForm from "../components/settings/PlayerProfileForm";
import BackButton from "../components/BackButton";
import ChangePasswordForm from "../components/settings/ChangePasswordForm";
import { userApi } from "../api/userApi";
import UserProfileForm from "../components/settings/UserProfileForm";
import { useUserProfile } from "../hooks/useUserProfile";
import RoleGuard from "../components/RoleGuard";
import { validatePlayerProfile } from "../validation/playerValidation";

import SuccessInfoModal from "../components/common/SuccessModal";

// Frontend validace AppUserDTO (profil uživatele)
const validateUserProfile = (values) => {
    const errors = {};

    if (!values.name || !values.name.trim()) {
        errors.name = "Křestní jméno je povinné.";
    } else if (
        values.name.trim().length < 2 ||
        values.name.trim().length > 50
    ) {
        errors.name = "Křestní jméno musí mít 2 až 50 znaků.";
    }

    if (!values.surname || !values.surname.trim()) {
        errors.surname = "Příjmení je povinné.";
    } else if (
        values.surname.trim().length < 2 ||
        values.surname.trim().length > 50
    ) {
        errors.surname = "Příjmení musí mít 2 až 50 znaků.";
    }

    // email tady nekontrolujeme – je jen read-only login

    return errors;
};


// Frontend validace kontaktních údajů v PlayerSettingsDTO
const validatePlayerSettingsContact = (values) => {
    const errors = {};

    const rawEmail = values.contactEmail ?? "";
    const rawPhone = values.contactPhone ?? "";

    const email = rawEmail.trim();
    const phone = rawPhone.trim();

    // 1) pokud chce emailové notifikace, email je povinný
    if (values.emailEnabled && !email) {
        errors.contactEmail =
            "Pro e-mailové notifikace musíš vyplnit kontaktní e-mail.";
    }

    // 2) pokud je email vyplněný, musí vypadat jako e-mail
    if (email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            errors.contactEmail =
                "Zadej platný e-mail ve formátu např. uzivatel@example.com.";
        }
    }

    // 3) pokud chce SMS notifikace, telefon je povinný
    if (values.smsEnabled && !phone) {
        errors.contactPhone =
            "Pro SMS notifikace musíš vyplnit kontaktní telefon.";
    }

    // 4) pokud je telefon vyplněný, musí být E.164
    if (phone) {
        const phoneRegex = /^\+[1-9]\d{11}$/;

        if (!phoneRegex.test(phone)) {
            errors.contactPhone =
                "Kontaktní telefon musí být v mezinárodním formátu a dostatečně dlouhý, např. +420123456789.";
        }
    }

    return errors;
};

// Frontend validace změny hesla
const validateChangePassword = (values) => {
    const errors = {};

    if (!values.oldPassword || !values.oldPassword.trim()) {
        errors.oldPassword = "Stávající heslo je povinné.";
    }

    if (!values.newPassword || !values.newPassword.trim()) {
        errors.newPassword = "Nové heslo je povinné.";
    } else if (values.newPassword.length < 8) {
        errors.newPassword = "Nové heslo musí mít alespoň 8 znaků.";
    }

    if (
        !values.newPasswordConfirm ||
        !values.newPasswordConfirm.trim()
    ) {
        errors.newPasswordConfirm = "Potvrzení nového hesla je povinné.";
    } else if (values.newPasswordConfirm !== values.newPassword) {
        errors.newPasswordConfirm = "Nové heslo a potvrzení se neshodují.";
    }

    if (
        values.oldPassword &&
        values.newPassword &&
        values.oldPassword === values.newPassword
    ) {
        errors.newPassword =
            "Nové heslo se musí lišit od stávajícího hesla.";
    }

    return errors;
};

const SettingsPage = () => {
    const [activeTab, setActiveTab] = useState("player"); // "player" | "user"

    // MODAL – stav + open/close
    const [successModal, setSuccessModal] = useState({
        show: false,
        title: "Hotovo",
        message: "",
    });

    const openSuccessModal = (message, title = "Hotovo") => {
        setSuccessModal({
            show: true,
            title,
            message,
        });
    };

    const closeSuccessModal = () => {
        setSuccessModal((prev) => ({ ...prev, show: false }));
    };

    // === Aktuální hráč (profil) ===
    const {
        currentPlayer,
        loading: loadingCurrentPlayer,
        error: errorCurrentPlayer,
    } = useCurrentPlayer();

    const emptyPlayerProfile = {
        id: null,
        name: "",
        surname: "",
        nickname: "",
        phoneNumber: "",
        team: "",
        primaryPosition: "",
        secondaryPosition: "",
    };

    const [playerProfileValues, setPlayerProfileValues] = useState(
        emptyPlayerProfile
    );
    const [savingProfile, setSavingProfile] = useState(false);
    const [profileError, setProfileError] = useState(null);
    const [profileSuccess, setProfileSuccess] = useState(null);
    const [profileFieldErrors, setProfileFieldErrors] = useState({});

    useEffect(() => {
        if (currentPlayer) {
            setPlayerProfileValues({
                id: currentPlayer.id,
                name: currentPlayer.name || "",
                surname: currentPlayer.surname || "",
                nickname: currentPlayer.nickname || "",
                phoneNumber: currentPlayer.phoneNumber || "",
                team: currentPlayer.team || "",
                primaryPosition: currentPlayer.primaryPosition || "",
                secondaryPosition: currentPlayer.secondaryPosition || "",

            });
        } else {
            setPlayerProfileValues(emptyPlayerProfile);
        }
        setProfileFieldErrors({});
        setProfileError(null);
        setProfileSuccess(null);
    }, [currentPlayer]);


    const handlePlayerProfileChange = (patch) => {
        setPlayerProfileValues((prev) => ({ ...prev, ...patch }));
        setProfileSuccess(null);

        const key = Object.keys(patch)[0];
        setProfileFieldErrors((prev) => {
            const copy = { ...prev };
            delete copy[key];
            return copy;
        });
    };

    const handlePlayerProfileSubmit = async () => {
        const errors = validatePlayerProfile(playerProfileValues);
        setProfileFieldErrors(errors);

        if (Object.keys(errors).length > 0) {
            setProfileError("Prosím zkontroluj zvýrazněná pole.");
            setProfileSuccess(null);
            return;
        }

        try {
            setSavingProfile(true);
            setProfileError(null);
            setProfileSuccess(null);

            const payload = {
                id: playerProfileValues.id,
                name: playerProfileValues.name?.trim(),
                surname: playerProfileValues.surname?.trim(),
                nickname: playerProfileValues.nickname?.trim() || null,
                phoneNumber:
                    playerProfileValues.phoneNumber &&
                        playerProfileValues.phoneNumber.trim() !== ""
                        ? playerProfileValues.phoneNumber.trim()
                        : null,
                team: playerProfileValues.team || null,
                primaryPosition: playerProfileValues.primaryPosition || null,
                secondaryPosition: playerProfileValues.secondaryPosition || null,
            };

            await updateMyCurrentPlayer(payload);

            const msg = "Profil hráče byl úspěšně uložen.";
            setProfileSuccess(msg);

            // ✅ MODAL
            openSuccessModal(msg, "Profil hráče");
        } catch (err) {
            const msg =
                err?.response?.data?.message ||
                "Nepodařilo se uložit profil hráče.";
            setProfileError(msg);
        } finally {
            setSavingProfile(false);
        }
    };

    // === Nastavení hráče (PlayerSettingsDTO – notifikace atd.) ===
    const {
        settings: playerSettings,
        loading: loadingPlayerSettings,
        saving: savingPlayerSettings,
        error: errorPlayerSettings,
        success: successPlayerSettings,
        saveSettings: savePlayerSettings,
    } = usePlayerSettings();

    const [playerSettingsValues, setPlayerSettingsValues] =
        useState(playerSettings);
    const [playerSettingsErrors, setPlayerSettingsErrors] = useState({});

    useEffect(() => {
        setPlayerSettingsValues(playerSettings);
        setPlayerSettingsErrors({});
    }, [playerSettings]);

    const handlePlayerSettingsChange = (patch) => {
        setPlayerSettingsValues((prev) => ({ ...prev, ...patch }));

        const key = Object.keys(patch)[0];
        setPlayerSettingsErrors((prev) => {
            const copy = { ...prev };
            delete copy[key];
            return copy;
        });
    };

    const handlePlayerSettingsSubmit = async () => {
        const errors = validatePlayerSettingsContact(playerSettingsValues);
        setPlayerSettingsErrors(errors);

        if (Object.keys(errors).length > 0) {
            return;
        }

        const emailRaw = playerSettingsValues.contactEmail ?? "";
        const phoneRaw = playerSettingsValues.contactPhone ?? "";

        const payload = {
            ...playerSettingsValues,
            contactEmail: emailRaw.trim() !== "" ? emailRaw.trim() : null,
            contactPhone: phoneRaw.trim() !== "" ? phoneRaw.trim() : null,
            reminderHoursBefore: playerSettingsValues.notifyReminders
                ? playerSettingsValues.reminderHoursBefore
                : null,
        };

        await savePlayerSettings(payload);

        // ✅ MODAL (nezasahuje do toho, co zobrazuje PlayerSettings komponenta)
        openSuccessModal(
            "Nastavení notifikací hráče bylo úspěšně uloženo.",
            "Notifikace hráče"
        );
    };

    // === Nastavení uživatele (AppUserSettingsDTO) ===
    const {
        settings: userSettings,
        loading: loadingUserSettings,
        saving: savingUserSettings,
        error: errorUserSettings,
        success: successUserSettings,
        saveSettings: saveUserSettings,
    } = useUserSettings();

    const [userFormValues, setUserFormValues] = useState(userSettings);

    useEffect(() => {
        setUserFormValues(userSettings);
    }, [userSettings]);

    const handleUserChange = (patch) => {
        setUserFormValues((prev) => ({ ...prev, ...patch }));
    };

    const handleUserSubmit = async () => {
        await saveUserSettings(userFormValues);

        // ✅ MODAL
        openSuccessModal(
            "Nastavení uživatelského účtu bylo úspěšně uloženo.",
            "Nastavení účtu"
        );
    };

    // === Změna hesla ===
    const [passwordValues, setPasswordValues] = useState({
        oldPassword: "",
        newPassword: "",
        newPasswordConfirm: "",
    });
    const [passwordErrors, setPasswordErrors] = useState({});
    const [passwordSaving, setPasswordSaving] = useState(false);
    const [passwordSuccess, setPasswordSuccess] = useState(null);
    const [passwordError, setPasswordError] = useState(null);

    const handlePasswordChange = (patch) => {
        setPasswordValues((prev) => ({ ...prev, ...patch }));
        setPasswordSuccess(null);

        const key = Object.keys(patch)[0];
        setPasswordErrors((prev) => {
            const copy = { ...prev };
            delete copy[key];
            return copy;
        });
    };

    const handlePasswordSubmit = async () => {
        const errors = validateChangePassword(passwordValues);
        setPasswordErrors(errors);

        if (Object.keys(errors).length > 0) {
            return;
        }

        try {
            setPasswordSaving(true);
            setPasswordError(null);
            setPasswordSuccess(null);

            const payload = {
                oldPassword: passwordValues.oldPassword,
                newPassword: passwordValues.newPassword,
                newPasswordConfirm: passwordValues.newPasswordConfirm,
            };

            const message = await userApi.changeMyPassword(payload); // ⬅️ tady

            const okMsg =
                typeof message === "string"
                    ? message
                    : "Heslo bylo úspěšně změněno.";

            setPasswordSuccess(okMsg);

            // ✅ MODAL
            openSuccessModal(okMsg, "Změna hesla");

            setPasswordValues({
                oldPassword: "",
                newPassword: "",
                newPasswordConfirm: "",
            });
        } catch (err) {
            const msg =
                err?.response?.data?.message ||
                "Nepodařilo se změnit heslo.";
            setPasswordError(msg);
        } finally {
            setPasswordSaving(false);
        }
    };

    // === Profil uživatele (AppUserDTO) ===
    const {
        profile: userProfile,
        loading: loadingUserProfile,
        saving: savingUserProfile,
        error: errorUserProfile,
        success: successUserProfile,
        saveProfile: saveUserProfile,
    } = useUserProfile();

    const emptyUserProfile = {
        id: null,
        name: "",
        surname: "",
        email: "",
        role: "",
        enabled: false,
        players: [],
    };

    const [userProfileValues, setUserProfileValues] = useState(emptyUserProfile);
    const [userProfileErrors, setUserProfileErrors] = useState({});

    useEffect(() => {
        if (userProfile) {
            setUserProfileValues({
                id: userProfile.id,
                name: userProfile.name || "",
                surname: userProfile.surname || "",
                email: userProfile.email || "",
                role: userProfile.role || "",
                enabled: userProfile.enabled ?? false,
                players: userProfile.players || [],
            });
        } else {
            setUserProfileValues(emptyUserProfile);
        }
        setUserProfileErrors({});
    }, [userProfile]);

    const handleUserProfileChange = (patch) => {
        setUserProfileValues((prev) => ({ ...prev, ...patch }));

        const key = Object.keys(patch)[0];
        setUserProfileErrors((prev) => {
            const copy = { ...prev };
            delete copy[key];
            return copy;
        });
    };

    const handleUserProfileSubmit = async () => {
        const errors = validateUserProfile(userProfileValues);
        setUserProfileErrors(errors);

        if (Object.keys(errors).length > 0) {
            return;
        }

        await saveUserProfile(userProfileValues);

        // ✅ MODAL
        openSuccessModal(
            "Profil uživatele byl úspěšně uložen.",
            "Profil uživatele"
        );
    };

    // Jednorázové doplnění kontaktního e-mailu z profilu uživatele,
    // pokud hráč žádný kontaktní e-mail ještě nemá
    useEffect(() => {
        if (!playerSettingsValues || !userProfile) return;

        const emailFromUser = (userProfile.email || "").trim();

        // pokud už něco v contactEmail je, nic nepřepisujeme
        if (
            playerSettingsValues.contactEmail &&
            playerSettingsValues.contactEmail.trim() !== ""
        ) {
            return;
        }

        if (emailFromUser) {
            setPlayerSettingsValues((prev) => ({
                ...prev,
                contactEmail: emailFromUser,
            }));
        }
    }, [playerSettingsValues, userProfile]);

    const isLoading =
        loadingCurrentPlayer ||
        loadingPlayerSettings ||
        loadingUserSettings ||
        loadingUserProfile;

    return (
        <div className="container mt-0">
            {/* MODAL – render */}
            <SuccessInfoModal
                show={successModal.show}
                title={successModal.title}
                message={successModal.message}
                onClose={closeSuccessModal}
            />

            <h1 className="h4 mb-3">Nastavení</h1>

            <p className="text-muted mb-3">
                Zde můžeš upravit nastavení svého uživatelského účtu i
                nastavení aktuálního hráče.
            </p>

            {/* Přepínač záložek */}
            <ul className="nav nav-pills mb-4 gap-2">
                <li className="nav-item">
                    <button
                        type="button"
                        className={
                            "nav-link border border-primary rounded " +
                            (activeTab === "player"
                                ? "active"
                                : "text-primary bg-transparent")
                        }
                        onClick={() => setActiveTab("player")}
                    >
                        Nastavení hráče
                    </button>
                </li>
                <li className="nav-item">
                    <button
                        type="button"
                        className={
                            "nav-link border border-primary rounded " +
                            (activeTab === "user"
                                ? "active"
                                : "text-primary bg-transparent")
                        }
                        onClick={() => setActiveTab("user")}
                    >
                        Nastavení uživatele (účtu)
                    </button>
                </li>
                <li className="nav-item">
                    <button
                        type="button"
                        className={
                            "nav-link border border-primary rounded " +
                            (activeTab === "password"
                                ? "active"
                                : "text-primary bg-transparent")
                        }
                        onClick={() => setActiveTab("password")}
                    >
                        Změna hesla
                    </button>
                </li>
                <RoleGuard roles={["ROLE_ADMIN"]}>
                    <li className="nav-item">
                        <button
                            type="button"
                            className={
                                "nav-link border border-primary rounded " +
                                (activeTab === "transfer"
                                    ? "active"
                                    : "text-primary bg-transparent")
                            }
                            onClick={() => setActiveTab("transfer")}
                        >
                            Přesun hráče k jinému uživateli
                        </button>
                    </li>
                </RoleGuard>
            </ul>

            {isLoading && <p>Načítám nastavení…</p>}

            {!isLoading && activeTab === "player" && (
                <>
                    {errorCurrentPlayer && (
                        <div className="alert alert-warning">
                            {errorCurrentPlayer}
                        </div>
                    )}

                    {!currentPlayer && !errorCurrentPlayer && (
                        <div className="alert alert-info">
                            Nemáš vybraného aktuálního hráče. Nastavení hráče
                            není k dispozici.
                        </div>
                    )}

                    {currentPlayer && (
                        <>
                            <div className="card mb-4 shadow-sm">
                                <div className="card-header bg-light">
                                    <strong>Profil hráče</strong>
                                </div>
                                <div className="card-body">
                                    {profileError && (
                                        <div className="alert alert-danger">
                                            {profileError}
                                        </div>
                                    )}
                                    {profileSuccess && (
                                        <div className="alert alert-success">
                                            {profileSuccess}
                                        </div>
                                    )}

                                    <PlayerProfileForm
                                        values={playerProfileValues}
                                        onChange={handlePlayerProfileChange}
                                        errors={profileFieldErrors}
                                    />
                                    <div className="d-flex justify-content-end">
                                        <button
                                            type="button"
                                            className="btn btn-primary"
                                            onClick={handlePlayerProfileSubmit}
                                            disabled={savingProfile}
                                        >
                                            {savingProfile
                                                ? "Ukládám profil hráče…"
                                                : "Uložit profil hráče"}
                                        </button>
                                    </div>
                                </div>
                            </div>

                            <div className="card mb-4 shadow-sm">
                                <div className="card-header bg-light">
                                    <strong>Nastavení notifikací hráče</strong>
                                </div>
                                <div className="card-body">
                                    <PlayerSettings
                                        values={playerSettingsValues}
                                        onChange={handlePlayerSettingsChange}
                                        onSubmit={handlePlayerSettingsSubmit}
                                        saving={savingPlayerSettings}
                                        error={errorPlayerSettings}
                                        success={successPlayerSettings}
                                        errors={playerSettingsErrors}
                                    />
                                </div>
                            </div>
                        </>
                    )}
                </>
            )}

            {!isLoading && activeTab === "user" && (
                <>
                    {/* Profil uživatele (AppUserDTO) */}
                    <div className="card mb-4 shadow-sm">
                        <div className="card-header bg-light d-flex justify-content-between align-items-center">
                            <strong>Profil uživatele</strong>
                        </div>
                        <div className="card-body">
                            {errorUserProfile && (
                                <div className="alert alert-danger">
                                    {errorUserProfile}
                                </div>
                            )}
                            {successUserProfile && (
                                <div className="alert alert-success">
                                    {successUserProfile}
                                </div>
                            )}

                            <UserProfileForm
                                values={userProfileValues}
                                onChange={handleUserProfileChange}
                                errors={userProfileErrors}
                            />

                            <div className="d-flex justify-content-end">
                                <button
                                    type="button"
                                    className="btn btn-primary"
                                    onClick={handleUserProfileSubmit}
                                    disabled={savingUserProfile}
                                >
                                    {savingUserProfile
                                        ? "Ukládám profil uživatele…"
                                        : "Uložit profil uživatele"}
                                </button>
                            </div>
                        </div>
                    </div>

                    {/* Nastavení uživatelského účtu (AppUserSettingsDTO) */}
                    <div className="card mb-4 shadow-sm">
                        <div className="card-header bg-light">
                            <strong>Nastavení uživatelského účtu</strong>
                        </div>
                        <div className="card-body">
                            <UserSettings
                                values={userFormValues}
                                onChange={handleUserChange}
                                onSubmit={handleUserSubmit}
                                saving={savingUserSettings}
                                error={errorUserSettings}
                                success={successUserSettings}
                            />
                        </div>
                    </div>
                </>
            )}

            {!isLoading && activeTab === "password" && (
                <div className="card mb-4 shadow-sm">
                    <div className="card-header bg-light">
                        <strong>Změna hesla</strong>
                    </div>
                    <div className="card-body">
                        <ChangePasswordForm
                            values={passwordValues}
                            errors={passwordErrors}
                            saving={passwordSaving}
                            success={passwordSuccess}
                            error={passwordError}
                            onChange={handlePasswordChange}
                            onSubmit={handlePasswordSubmit}
                        />
                    </div>
                </div>
            )}
        </div>
    );
};

export default SettingsPage;