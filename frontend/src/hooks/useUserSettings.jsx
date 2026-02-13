// src/hooks/useUserSettings.js
import { useEffect, useState, useCallback } from "react";
import { getUserSettings, updateUserSettings } from "../api/userSettingsApi";

const emptyUserSettings = {
    playerSelectionMode: "FIRST_PLAYER",
    globalNotificationLevel: "ALL",
    copyAllPlayerNotificationsToUserEmail: false,
    receiveNotificationsForPlayersWithOwnEmail: false,
    emailDigestEnabled: false,
    emailDigestTime: "20:00",
    uiLanguage: "cs",
    timezone: "Europe/Prague",
    defaultLandingPage: "MATCHES",
};

export const useUserSettings = () => {
    const [settings, setSettings] = useState(emptyUserSettings);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    useEffect(() => {
        let isMounted = true;

        const load = async () => {
            try {
                setLoading(true);
                setError(null);
                const data = await getUserSettings();
                if (!isMounted) return;
                setSettings(data || emptyUserSettings);
            } catch (err) {
                if (!isMounted) return;
                const msg =
                    err?.response?.data?.message ||
                    "Nepodařilo se načíst nastavení uživatele.";
                setError(msg);
            } finally {
                if (isMounted) setLoading(false);
            }
        };

        load();

        return () => {
            isMounted = false;
        };
    }, []);

    const saveSettings = useCallback(
        async (newSettings) => {
            try {
                setSaving(true);
                setError(null);
                setSuccess(null);

                const payload = { ...settings, ...newSettings };
                const updated = await updateUserSettings(payload);

                setSettings(updated);
                setSuccess("Nastavení uživatele bylo úspěšně uloženo.");
                return updated;
            } catch (err) {
                const msg =
                    err?.response?.data?.message ||
                    "Nepodařilo se uložit nastavení uživatele.";
                setError(msg);
                throw err;
            } finally {
                setSaving(false);
            }
        },
        [settings]
    );

    return {
        settings,
        setSettings,
        loading,
        saving,
        error,
        success,
        saveSettings,
    };
};
