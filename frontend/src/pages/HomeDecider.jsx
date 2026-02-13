// src/pages/HomeDecider.jsx

import { useAuth } from "../hooks/useAuth";
import Home from "./Home";
import AdminHomePage from "./AdminHomePage";

const HomeDecider = () => {
    const { user, loading } = useAuth();

    if (loading) {
        return <p>Načítám uživatele…</p>;
    }

    // Defenzivně znormalizujeme role
    const rawRoles = user?.roles || user?.role || [];
    const roles = Array.isArray(rawRoles) ? rawRoles : [rawRoles].filter(Boolean);

    const isAdmin = roles.includes("ROLE_ADMIN");

    if (isAdmin) {
        return <AdminHomePage />;
    }

    // ostatní (hráč, manažer…) uvidí původní Home
    return <Home />;
};

export default HomeDecider;
