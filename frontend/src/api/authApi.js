import api from "./axios";

/**
 * Přihlášení uživatele
 */
export const loginUser = (email, password) =>
    api.post("auth/login", { email, password });

/**
 * Odhlášení
 */
export const logoutUser = () =>
    api.post("auth/logout");

/**
 * Načtení aktuálního uživatele
 */
export const fetchCurrentUser = () =>
    api.get("/auth/me");

/**
 * Rychlá kontrola přihlášení
 */
export const checkAuthentication = async () => {
    try {
        await fetchCurrentUser();
        return true;
    } catch {
        return false;
    }
};

/**
 * Registrace nového uživatele
 */
export const registerUser = (data) =>
    api.post("/auth/register", data);

/**
 * Aktivace účtu pomocí tokenu
 */
export const verifyEmail = (token) =>
    api.get("/auth/verify", { params: { token } });


/**
 * Zapomenuté heslo – požadavek na reset (pošle e-mail s odkazem)
 */
export const requestForgottenPassword = (email) =>
    api.post("/auth/forgotten-password", { email });

/**
 * Zapomenuté heslo – zjistí info podle tokenu (e-mail)
 */
export const getForgottenPasswordInfo = (token) =>
    api.get("/auth/forgotten-password/info", {
        params: { token },
    });

/**
 * Zapomenuté heslo – finální reset hesla
 */
export const resetForgottenPassword = (data) =>
    api.post("/auth/forgotten-password/reset", data);