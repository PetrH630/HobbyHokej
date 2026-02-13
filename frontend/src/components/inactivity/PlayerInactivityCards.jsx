// src/components/inactivity/PlayerInactivityCards.jsx
import React from "react";

const formatDateTime = (iso) => {
    if (!iso) return "";
    try {
        const d = new Date(iso);
        return d.toLocaleString("cs-CZ", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
            hour: "2-digit",
            minute: "2-digit",
        });
    } catch {
        return iso;
    }
};

const PlayerInactivityCards = ({ periods, loading, error }) => {
    if (loading) {
        return <p>Načítám období neaktivity…</p>;
    }

    if (error) {
        return <div className="alert alert-danger">{error}</div>;
    }

    if (!periods || periods.length === 0) {
        return <p>Nemáte evidována žádná období neaktivity.</p>;
    }

    return (
        <div className="mt-4">
            <h2 className="h5 mb-3">Období neaktivity</h2>
            <div className="row g-3">
                {periods.map((p) => (
                    <div className="col-md-6" key={p.id}>
                        <div className="card shadow-sm h-100">
                            <div className="card-body">
                                <h5 className="card-title mb-2">
                                    Neaktivní
                                </h5>
                                <p className="mb-1">
                                    <strong>Od:</strong>{" "}
                                    {formatDateTime(p.inactiveFrom)}
                                </p>
                                <p className="mb-1">
                                    <strong>Do:</strong>{" "}
                                    {formatDateTime(p.inactiveTo)}
                                </p>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default PlayerInactivityCards;
