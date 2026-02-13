// src/hooks/useDemoNotifications.js
import { useCallback, useState } from "react";
import { getDemoNotifications } from "../api/demoNotificationsApi";

export const useDemoNotifications = () => {
    const [notifications, setNotifications] = useState({
        emails: [],
        sms: [],
    });
    const [showModal, setShowModal] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const loadAndShow = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);

            const response = await getDemoNotifications();
            const data = response.data || {};

            const emails = data.emails ?? [];
            const sms = data.sms ?? [];

            const hasAny = (emails && emails.length > 0) || (sms && sms.length > 0);

            if (hasAny) {
                setNotifications({ emails, sms });
                setShowModal(true);
                
                // debug do konzole
                console.log("DEMO notifications loaded:", { emails, sms });
                return { hasAny, emails, sms };
            } else {
                setNotifications({ emails: [], sms: [] });
                setShowModal(false);
                console.log("DEMO notifications: prázdné");
            }
        } catch (e) {
            console.error("Nepodařilo se načíst demo notifikace:", e);
            setError("Nepodařilo se načíst demo notifikace.");
        } finally {
            setLoading(false);
        }
    }, []);

    const close = useCallback(() => {
        setShowModal(false);
    }, []);

    return {
        notifications,
        showModal,
        loading,
        error,
        loadAndShow,
        close,
    };
};
