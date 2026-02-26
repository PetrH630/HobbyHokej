// src/hooks/usePlayerSettings.js
import { useEffect, useState, useCallback } from "react";
import {
    getCurrentPlayerSettings,
    updateCurrentPlayerSettings,
    getPlayerSettings,
    updatePlayerSettings,
} from "../api/playerSettingsApi";

const emptySettings = {
    contactEmail: "",
    contactPhone: "",
    emailEnabled: false,
    smsEnabled: false,
    notifyOnRegistration: true,
    notifyOnExcuse: true,
    notifyOnMatchChange: true,
    notifyOnMatchCancel: true,
    notifyOnPayment: false,
    notifyReminders: true,
    reminderHoursBefore: 24,

    // 🔹 nové položky z PlayerSettingsDTO
    possibleMoveToAnotherTeam: false,
    possibleChangePlayerPosition: false,
};

export const usePlayerSettings = (playerId = null) => {
    const [settings, setSettings] = useState(emptySettings);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    // načtení
    useEffect(() => {
        let isMounted = true;

        const load = async () => {
            try {
                setLoading(true);
                setError(null);

                const data = playerId
                    ? await getPlayerSettings(playerId)
                    : await getCurrentPlayerSettings();

                if (!isMounted) return;

                // 🔹 zajistí, že pokud backend některou hodnotu neposlal,
                // doplní se z emptySettings
                setSettings({
                    ...emptySettings,
                    ...(data || {}),
                });
            } catch (err) {
                if (!isMounted) return;

                console.error(
                    "load player settings error:",
                    err?.response || err
                );

                const msg =
                    err?.response?.data?.message ||
                    "Nepodařilo se načíst nastavení hráče.";
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
    }, [playerId]);

    // ukládání
    const saveSettings = useCallback(
        async (newSettings) => {
            try {
                setSaving(true);
                setError(null);
                setSuccess(null);

                // newSettings už bude "payload" z SettingsPage
                const payload = { ...settings, ...newSettings };

                const updated = playerId
                    ? await updatePlayerSettings(playerId, payload)
                    : await updateCurrentPlayerSettings(payload);

                setSettings({
                    ...emptySettings,
                    ...(updated || {}),
                });
                setSuccess("Nastavení bylo úspěšně uloženo.");
                return updated;
            } catch (err) {
                console.error(
                    "save player settings error:",
                    err?.response?.status,
                    err?.response?.data,
                    err
                );

                const msg =
                    err?.response?.data?.message ||
                    "Nepodařilo se uložit nastavení hráče.";
                setError(msg);
                throw err;
            } finally {
                setSaving(false);
            }
        },
        [playerId, settings]
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