
import { createContext, useContext, useEffect, useState } from "react";
import { fetchCurrentUser, logoutUser } from "../api/authApi";

const AuthContext = createContext(null);

/**
 * AuthProvider a useAuth
 *
 * Centrální autentizační vrstva frontendu.
 * Provider drží v paměti přihlášeného uživatele a stav načítání a poskytuje API pro:
 * - načtení aktuálního uživatele z backendu (`updateUser`)
 * - odhlášení (`logout`)
 *
 * Vedlejší efekty:
 * - při mountu provideru se jednorázově volá `fetchCurrentUser()` a naplní se `user`
 */
export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    const loadUser = async () => {
        setLoading(true);
        try {
            const res = await fetchCurrentUser();
            setUser(res.data);
        } catch {
            setUser(null);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadUser();
    }, []);

    const updateUser = async () => {
        await loadUser();
    };

    const logout = async () => {
        try {
            await logoutUser();
        } catch (err) {
            console.error("Logout error:", err);
        } finally {
            // Po odhlášení se lokální stav uživatele vynuluje, aby se UI okamžitě přepnulo do anonymního režimu.
            setUser(null);
        }
    };

    const isAuthenticated = !!user;

    return (
        <AuthContext.Provider
            value={{
                user,
                loading,
                isAuthenticated,
                updateUser,
                logout,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth must be used inside AuthProvider");
    }
    return context;
};
