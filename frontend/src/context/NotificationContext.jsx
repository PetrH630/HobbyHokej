import { createContext, useContext, useState } from "react";

const NotificationContext = createContext(null);

/**
 * NotificationContext
 *
 * Lehký React kontext pro sdílení notifikací a akcí nad nimi napříč aplikací.
 * Komponenty jej používají přes `useNotificationContext()` místo prop-drillingu.
 */
export const NotificationProvider = ({ children }) => {
    const [notification, setNotification] = useState(null);

    const showNotification = (message, type = "success", timeout = 4000) => {
        setNotification({ message, type });
        if (timeout) {
            setTimeout(() => setNotification(null), timeout);
        }
    };

    const clearNotification = () => setNotification(null);

    return (
        <NotificationContext.Provider
            value={{ notification, showNotification, clearNotification }}
        >
            {children}
        </NotificationContext.Provider>
    );
};

export const useNotification = () => useContext(NotificationContext);
