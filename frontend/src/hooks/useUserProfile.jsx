// src/hooks/useUserProfile.js
import { useEffect, useState } from "react";
import { userApi } from "../api/userApi";

/**
 * Práce s profilem přihlášeného uživatele (AppUserDTO).
 *
 * Načítá /api/users/me a ukládá přes /api/users/me/update.
 */
export const useUserProfile = () => {
    const [profile, setProfile] = useState(null); // AppUserDTO
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    // načtení profilu při mountu
    useEffect(() => {
        let isMounted = true;

        const load = async () => {
            try {
                setLoading(true);
                setError(null);
                const data = await userApi.getCurrent(); // GET /users/me
                if (!isMounted) return;
                setProfile(data);
            } catch (err) {
                if (!isMounted) return;
                const msg =
                    err?.response?.data?.message ||
                    "Nepodařilo se načíst profil uživatele.";
                setError(msg);
            } finally {
                if (isMounted) {
                    setLoading(false);
                }
            }
        };

        load();

        return () => {
            isMounted = false;
        };
    }, []);

    // uložení profilu
    const saveProfile = async (updatedProfile) => {
        try {
            setSaving(true);
            setError(null);
            setSuccess(null);

            // voláme backend – PUT /api/users/me/update (AppUserDTO)
            const dto = {
                id: updatedProfile.id,
                name: updatedProfile.name?.trim(),
                surname: updatedProfile.surname?.trim(),
                email: updatedProfile.email, // login – neměníme, ale DTO ho očekává
                role: updatedProfile.role,
                enabled: updatedProfile.enabled,
                players: updatedProfile.players,
            };

            const message = await userApi.updateCurrent(dto);

            setProfile(dto); // lokálně přepíšeme
            setSuccess(
                typeof message === "string"
                    ? message
                    : "Profil uživatele byl úspěšně uložen."
            );
        } catch (err) {
            const msg =
                err?.response?.data?.message ||
                "Nepodařilo se uložit profil uživatele.";
            setError(msg);
            throw err;
        } finally {
            setSaving(false);
        }
    };

    return {
        profile,
        loading,
        saving,
        error,
        success,
        saveProfile,
        setProfile, // když budeš chtít manipulovat ručně
        setError,
        setSuccess,
    };
};
