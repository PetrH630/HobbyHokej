// src/components/notifications/NotificationCard.jsx
import { formatDistanceToNow } from "date-fns";
import { cs } from "date-fns/locale";

const NotificationCard = ({ notification, onMarkRead }) => {
    if (!notification) return null;

    const {
        id,
        messageShort,
        messageFull,
        createdAt,
        read,
        important,
        player,
    } = notification;

    const createdDate = createdAt ? new Date(createdAt) : null;
    const createdRelative =
        createdDate && !Number.isNaN(createdDate.getTime())
            ? formatDistanceToNow(createdDate, {
                addSuffix: true,
                locale: cs,
            })
            : null;

    const rootClass =
        "card mb-2 " +
        (read ? "border-light bg-light" : important ? "border-danger" : "border-primary");

    const renderBadges = () => (
        <>
            {!read && (
                <span className="badge bg-primary d-block mb-1">
                    Nové
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
                <div className="d-flex justify-content-between align-items-start mb-1">
                    <div className="flex-grow-1">

                        {/* Badge nad textem na malém zařízení */}
                        <div className="d-sm-none mb-1 text-end">
                            {renderBadges()}
                        </div>

                        <div className="fw-semibold">
                            {messageShort || "Notifikace"}
                        </div>

                        {messageFull && (
                            <div className="text-muted small mt-1">
                                {messageFull}
                            </div>
                        )}

                        {player && (
                            <div className="small mt-1">
                                <span className="text-muted">Hráč:&nbsp;</span>
                                <span>
                                    {player.fullName ||
                                        `${player.name ?? ""} ${player.surname ?? ""}`}
                                </span>
                            </div>
                        )}

                        {/* ✅ TLAČÍTKO POD TEXTEM NA MALÉM ZAŘÍZENÍ */}
                        {!read && onMarkRead && (
                            <div className="d-sm-none mt-2">
                                <button
                                    type="button"
                                    className="btn btn-sm btn-outline-secondary w-100"
                                    onClick={() => onMarkRead(id)}
                                >
                                    Označit jako přečtené
                                </button>
                            </div>
                        )}
                    </div>

                    {/* Badge vpravo na ≥ sm */}
                    <div className="text-end ms-2 d-none d-sm-block">
                        {renderBadges()}
                    </div>
                </div>

                {/* Spodní řádek */}
                <div className="d-flex justify-content-between align-items-center mt-2">
                    <div className="text-muted small">
                        {createdRelative
                            ? `Vytvořeno ${createdRelative}`
                            : createdAt}
                    </div>

                    {/* ✅ TLAČÍTKO VPRAVO NA ≥ sm */}
                    {!read && onMarkRead && (
                        <div className="d-none d-sm-block">
                            <button
                                type="button"
                                className="btn btn-sm btn-outline-secondary"
                                onClick={() => onMarkRead(id)}
                            >
                                Označit jako přečtené
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default NotificationCard;