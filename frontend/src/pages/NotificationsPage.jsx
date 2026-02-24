// src/pages/NotificationsPage.jsx
import { useNotifications } from "../hooks/useNotifications";
import NotificationsList from "../components/notifications/NotificationsList";
import BackButton from "../components/BackButton";

const NotificationsPage = () => {
    const {
        notifications,
        loading,
        error,
        markOneAsRead,
        markAllAsRead,
    } = useNotifications({
        mode: "sinceLastLogin",
        limit: 200,
    });

    return (
        <div className="container py-3">
            <div className="row mb-3">
                <div className="col">
                    <h1 className="h4 mb-0">Notifikace</h1>
                    <p className="text-muted mb-0 small">
                        Zobrazeny jsou notifikace od posledního přihlášení.
                    </p>
                    <BackButton />
                </div>
            </div>

            {loading && <p>Načítám notifikace…</p>}

            {error && (
                <div className="alert alert-danger" role="alert">
                    {error}
                </div>
            )}

            {!loading && !error && (
                <NotificationsList
                    notifications={notifications}
                    onMarkOneAsRead={markOneAsRead}
                    onMarkAllAsRead={markAllAsRead}
                />
            )}
        </div>
    );
};

export default NotificationsPage;