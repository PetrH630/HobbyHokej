import { formatDistanceToNow } from "date-fns";
import { cs } from "date-fns/locale";

/**
 * AdminNotificationCard
 *
 * Karta pro zobrazení přehledových informací a akcí nad konkrétní entitou.
 *
 * Props:
 * @param {Object} props.group vstupní hodnota komponenty.
 */
const AdminNotificationCard = ({ group }) => {
    if (!group) return null;

    const { notifications, createdAt, important } = group;

    if (!notifications || notifications.length === 0) {
        return null;
    }

    const first = notifications[0];

    const messageShort = first.messageShort;
    const messageFull = first.messageFull;

    const createdDate = createdAt ? new Date(createdAt) : null;
    const createdRelative =
        createdDate && !Number.isNaN(createdDate.getTime())
            ? formatDistanceToNow(createdDate, {
                addSuffix: true,
                locale: cs,
            })
            : null;

    const isNotificationRead = (n) => {
        if (n.read === true) return true;
        if (n.read === "true") return true;
        if (n.readAt != null) return true;
        return false;
    };

    const hasUnread = notifications.some((n) => !isNotificationRead(n));
    const allRead =
        notifications.length > 0 &&
        notifications.every((n) => isNotificationRead(n));

    const rootClass =
        "card mb-2 " +
        (allRead
            ? "border-light bg-light"
            : important
                ? "border-danger"
                : "border-primary");


    const recipients = notifications.map((n) => {
        const { player, readAt, emailTo, smsTo } = n;

        const playerName =
            player?.fullName ||
            `${player?.name ?? ""} ${player?.surname ?? ""}`.trim() ||
            null;

        const readDate = readAt ? new Date(readAt) : null;
        const readRelative =
            readDate && !Number.isNaN(readDate.getTime())
                ? formatDistanceToNow(readDate, {
                    addSuffix: true,
                    locale: cs,
                })
                : null;

        const isRead = isNotificationRead(n);

        return {
            id: n.id,
            playerName,
            emailTo: emailTo || null,
            smsTo: smsTo || null,
            isRead,
            readAt,
            readRelative,
        };
    });

    const renderStatusBadges = () => (
        <>
            {hasUnread && (
                <span className="badge bg-secondary d-block mb-1">
                    Některé nepřečtené
                </span>
            )}
            {allRead && (
                <span className="badge bg-success d-block mb-1">
                    Vše přečtené
                </span>
            )}
            {important && (
                <span className="badge bg-danger d-block">
                    Důležité
                </span>
            )}
        </>
    );

    return (
        <div className={rootClass}>
            <div className="card-body">

                <div className="d-flex justify-content-between align-items-start mb-2">
                    <div className="me-3 flex-grow-1">

                        <div className="d-sm-none mb-1 text-end">
                            {renderStatusBadges()}
                        </div>

                        <div className="fw-semibold">
                            {messageShort || "Notifikace"}
                        </div>

                        {messageFull && (
                            <div className="text-muted small mt-1">
                                {messageFull}
                            </div>
                        )}
                    </div>

                    <div className="text-end ms-2 d-none d-sm-block">
                        {renderStatusBadges()}
                    </div>
                </div>

                <div className="mt-2">
                    <div className="small text-muted mb-1">
                        Příjemci ({recipients.length}):
                    </div>
                    <ul className="list-unstyled small mb-0">
                        {recipients.map((r) => (
                            <li
                                key={r.id}
                                className={`mb-2 p-2 rounded ${r.isRead ? "bg-success bg-opacity-10 border border-success" : ""
                                    }`}
                            >
                                <div className="me-2">
                                    <div>
                                        <span className="fw-semibold">
                                            Hráč:{" "}
                                        </span>
                                        {r.playerName || "—"}
                                    </div>
                                    <div>
                                        <span className="fw-semibold">
                                            E-mail:{" "}
                                        </span>
                                        {r.emailTo || "—"}
                                    </div>
                                    <div>
                                        <span className="fw-semibold">
                                            Tel.:{" "}
                                        </span>
                                        {r.smsTo || "—"}
                                    </div>

                                    {/* Stav */}
                                    <div className="mt-1">
                                        {r.isRead ? (
                                            <span className="text-success fw-semibold">
                                                {r.readRelative
                                                    ? `Přečteno ${r.readRelative}`
                                                    : r.readAt
                                                        ? `Přečteno ${r.readAt}`
                                                        : "Přečteno"}
                                            </span>
                                        ) : (
                                            <span className="badge bg-secondary">
                                                Nepřečtená
                                            </span>
                                        )}
                                    </div>
                                </div>
                            </li>
                        ))}
                    </ul>
                </div>

                <div className="d-flex justify-content-between align-items-center mt-3">
                    <div className="text-muted small">
                        {createdRelative
                            ? `Vytvořeno ${createdRelative}`
                            : createdAt}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminNotificationCard;