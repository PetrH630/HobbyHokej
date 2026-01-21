// src/hooks/useAuth.jsx

import { createContext, useContext, useEffect, useState } from "react";
import { fetchCurrentUser, logoutUser } from "../api/authApi";

const AuthContext = createContext(null);

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
            setUser(null);
        }
    };

    return (
        <AuthContext.Provider value={{ user, loading, updateUser, logout }}>
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
