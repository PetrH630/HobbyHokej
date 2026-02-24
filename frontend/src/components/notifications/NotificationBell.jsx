// src/components/notifications/NotificationBell.jsx
import { useNavigate } from "react-router-dom";
import { FaBell } from "react-icons/fa";
import { useNotificationBadge } from "../../hooks/useNotificationBadge";

/**
 * Zvoneček s badge pro nové nepřečtené notifikace od posledního přihlášení.
 *
 * Hodnota se bere z badge.unreadCountSinceLastLogin,
 * kterou by měl dodat backend / API.
 *
 * Slouží zároveň jako odkaz na stránku /app/notifications.
 */
const NotificationBell = () => {
    const navigate = useNavigate();
    const { badge, loading, error } = useNotificationBadge() || {};

    const newUnreadCount = badge?.unreadCountSinceLastLogin ?? 0;
    const displayCount = newUnreadCount > 99 ? "99+" : newUnreadCount;

    const handleClick = () => {
        navigate("/app/notifications");
    };

    // Když se badge ještě načítá / chyba, pořád zobrazujeme zvonek
    // jen bez/nebo s nulovým badge
    return (
        <button
            type="button"
            className="btn btn-link position-relative p-0 me-2"
            onClick={handleClick}
            aria-label={
                newUnreadCount > 0
                    ? `Máte ${newUnreadCount} nových nepřečtených notifikací od posledního přihlášení`
                    : "Notifikace"
            }
            title={
                newUnreadCount > 0
                    ? `Nové nepřečtené notifikace: ${newUnreadCount}`
                    : "Notifikace"
            }
        >
            <FaBell size={20} />

            {newUnreadCount > 0 && (
                <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
                    {displayCount}
                    <span className="visually-hidden">
                        {" "}
                        nových nepřečtených notifikací od posledního přihlášení
                    </span>
                </span>
            )}
        </button>
    );
};

export default NotificationBell;