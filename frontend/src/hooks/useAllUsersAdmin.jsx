// src/hooks/useAllUsersAdmin.js
import { useEffect, useState } from "react";
import { userApi } from "../api/userApi";

/**
 * Hook pro globální správu uživatelů (ADMIN).
 *
 * Načítá všechny uživatele v systému přes GET /api/users.
 * Vrací:
 *  - users: pole všech uživatelů (AppUserDTO[])
 *  - loading: true, dokud se načítá
 *  - error: text chyby nebo null
 *  - reload(): znovu načte data (po editaci / aktivaci / deaktivaci / resetu hesla)
 */
export const useAllUsersAdmin = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const loadUsers = async () => {
        setLoading(true);
        setError(null);

        try {
            const data = await userApi.getAll(); // už používáš v hooku useAllPlayersAdminWithUsers
            setUsers(data);
            return data;
        } catch (err) {
            console.error("Nepodařilo se načíst uživatele", err);

            const message =
                err?.response?.data?.message ||
                err?.message ||
                "Nepodařilo se načíst uživatele";

            setError(message);
            return null;
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        // načtení při prvním zobrazení admin stránky
        loadUsers();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return {
        users,
        loading,
        error,
        reload: loadUsers,
    };
};
