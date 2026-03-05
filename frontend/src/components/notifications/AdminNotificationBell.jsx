import { NavLink } from "react-router-dom";
import { useNotificationBadge } from "../../hooks/useNotificationBadge";
import { Bell } from "react-bootstrap-icons";

/**
 * AdminNotificationBell
 *
 * Komponenta pro zobrazení notifikací a práci se stavem přečtení.
 *
 * @param {Object} props vstupní hodnoty komponenty.
 */
const AdminNotificationBell = () => {
    const { badge } = useNotificationBadge() || {};
    const unreadCount = badge?.unreadCount ?? 0;

    return (
        <NavLink
            to="/app/admin/notifications"
            className="btn btn-sm btn-outline-warning position-relative d-inline-flex align-items-center justify-content-center"
            title="Notifikace pro správce"
        >
            <Bell size={18} />

            {unreadCount > 0 && (
                <span
                    className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
                    style={{ fontSize: "0.7rem" }}
                >
                    {unreadCount > 99 ? "99+" : unreadCount}
                </span>
            )}
        </NavLink>
    );
};

export default AdminNotificationBell;