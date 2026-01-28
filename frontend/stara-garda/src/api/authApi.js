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
