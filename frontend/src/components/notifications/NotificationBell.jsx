import { useNavigate } from "react-router-dom";
import { FaBell } from "react-icons/fa";
import { useNotificationBadge } from "../../hooks/useNotificationBadge";

/**
 * NotificationBell
 *
 * Komponenta pro zobrazení notifikací a práci se stavem přečtení.
 *
 * @param {Object} props vstupní hodnoty komponenty.
 */
const NotificationBell = () => {
    const navigate = useNavigate();
    const { badge, loading, error } = useNotificationBadge() || {};

    const newUnreadCount = badge?.unreadCountSinceLastLogin ?? 0;
    const displayCount = newUnreadCount > 99 ? "99+" : newUnreadCount;

    
    const handleClick = () => {
        navigate("/app/notifications");
    };


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