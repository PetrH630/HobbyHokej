// src/pages/admin/AdminNotificationPage.jsx
import AdminNotificationsList from "../components/notifications/AdminNotificationList";

/**
 * Stránka pro administrátorský přehled systémových notifikací.
 *
 * Zobrazuje kompletní seznam notifikací v systému
 * (pro roli ADMIN / MANAGER).
 *
 * Komponenta je pouze prezentační – logika načítání dat
 * je zapouzdřena v AdminNotificationsList.
 */
const AdminNotificationPage = () => {
    return (
        <div className="container py-4">
            <div className="mb-4">
                <h1 className="h3 mb-1">Systémové notifikace</h1>
                <p className="text-muted mb-0">
                    Přehled všech notifikací v systému (read-only).
                </p>
            </div>

            <AdminNotificationsList />
        </div>
    );
};

export default AdminNotificationPage;