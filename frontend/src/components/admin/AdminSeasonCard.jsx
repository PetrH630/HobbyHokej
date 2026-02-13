// src/components/admin/AdminSeasonCard.jsx
import RoleGuard from "../RoleGuard";

const formatDate = (value) => {
    if (!value) return "-";
    try {
        return new Date(value).toLocaleDateString("cs-CZ");
    } catch {
        return value;
    }
};

const AdminSeasonCard = ({ season, onEdit, onSetActive }) => {
    const isActive = season.active === true;

    return (
        <div className="card shadow-sm">
            <div className="card-body">
                <div className="row align-items-center">
                    <div className="col-md-4 fw-bold">
                        {season.name || "Bez názvu"}
                        {isActive && (
                            <span className="badge bg-success ms-2">
                                aktivní
                            </span>
                        )}
                    </div>

                    <div className="col-md-4">
                        <small className="text-muted d-block">
                            Období sezóny
                        </small>
                        {formatDate(season.startDate)} –{" "}
                        {formatDate(season.endDate)}
                    </div>

                    <div className="col-md-4">
                        <RoleGuard roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
                            <div className="d-flex justify-content-end">
                                <div className="btn-group btn-group-sm">
                                    {/* UPRAVIT */}
                                    <button
                                        type="button"
                                        className="btn btn-primary"
                                        disabled={!onEdit}
                                        onClick={() =>
                                            onEdit && onEdit(season)
                                        }
                                    >
                                        Upravit
                                    </button>

                                    {/* NASTAVIT AKTIVNÍ */}
                                    <button
                                        type="button"
                                        className={
                                            "btn btn-outline-success" + (isActive ? " disabled" : "")
                                        }
                                        disabled={isActive || !onSetActive}
                                        title={
                                            isActive
                                                ? "Tato sezóna je již aktivní"
                                                : "Nastavit tuto sezónu jako aktivní"
                                        }
                                        onClick={() =>
                                            !isActive && onSetActive && onSetActive(season.id)
                                        }
                                    >
                                        Nastavit aktivní
                                    </button>

                                </div>
                            </div>
                        </RoleGuard>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminSeasonCard;
