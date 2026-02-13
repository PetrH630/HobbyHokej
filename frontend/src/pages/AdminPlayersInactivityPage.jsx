// src/pages/AdminPlayersInactivityPage.jsx
import { useMemo, useState } from "react";
import { usePlayerInactivityPeriodsAdmin } from "../hooks/usePlayerInactivityPeriodsAdmin";
import { useAllPlayersAdmin } from "../hooks/useAllPlayersAdmin";
import AdminPlayerInactivityModal from "../components/admin/AdminPlayerInactivityModal";

const normalizeDate = (value) => {
    if (!value) return null;

    const raw =
        typeof value === "string"
            ? value.includes("T")
                ? value
                : value.replace(" ", "T")
            : value;

    const d = new Date(raw);
    return Number.isNaN(d.getTime()) ? null : d;
};

const formatDateTime = (value) => {
    const d = normalizeDate(value);
    if (!d) return "-";
    return d.toLocaleString("cs-CZ");
};

const FILTERS = {
    ALL: "ALL",
    CURRENT: "CURRENT",
    NON_CURRENT: "NON_CURRENT",
};

const AdminPlayersInactivityPage = () => {
    const {
        periods,
        loading: inactivityLoading,
        error: inactivityError,
        reload: reloadInactivity,
    } = usePlayerInactivityPeriodsAdmin();

    const {
        players,
        loading: playersLoading,
        error: playersError,
        reload: reloadPlayers,
    } = useAllPlayersAdmin();

    // stav pro modal s neaktivitou hráče
    const [selectedPlayer, setSelectedPlayer] = useState(null);
    const [showInactivityModal, setShowInactivityModal] = useState(false);

    const [filter, setFilter] = useState(FILTERS.ALL);

    const openInactivityModal = (player) => {
        if (!player) return;
        setSelectedPlayer(player);
        setShowInactivityModal(true);
    };

    const closeInactivityModal = () => {
        setShowInactivityModal(false);
        setSelectedPlayer(null);
    };

    const handleReloadAll = () => {
        reloadInactivity();
        reloadPlayers();
    };

    // Mapa playerId -> player
    const playerMap = useMemo(() => {
        const map = new Map();
        if (players && players.length > 0) {
            players.forEach((p) => {
                if (p.id != null) {
                    map.set(p.id, p);
                }
            });
        }
        return map;
    }, [players]);

    // Seřadíme období podle příjmení hráče, pak podle začátku neaktivity
    // a zároveň každému období spočítáme, jestli je aktuální (isCurrent)
    const sortedPeriods = useMemo(() => {
        if (!periods || periods.length === 0) return [];

        const now = new Date();

        const withCurrentFlag = periods.map((p) => {
            const from = normalizeDate(p.inactiveFrom);
            const to = normalizeDate(p.inactiveTo);

            const isCurrent =
                from && to ? from <= now && to >= now : false;

            return {
                ...p,
                isCurrent,
            };
        });

        return withCurrentFlag.slice().sort((a, b) => {
            const playerA = playerMap.get(a.playerId);
            const playerB = playerMap.get(b.playerId);

            const surnameA = (playerA?.surname || "").toLocaleLowerCase("cs");
            const surnameB = (playerB?.surname || "").toLocaleLowerCase("cs");

            // 1) příjmení (neznámí hráči až na konec)
            if (!surnameA && surnameB) return 1;
            if (surnameA && !surnameB) return -1;

            const cmpSurname = surnameA.localeCompare(surnameB, "cs", {
                sensitivity: "base",
            });
            if (cmpSurname !== 0) return cmpSurname;

            // 2) začátek neaktivity
            const fromA = normalizeDate(a.inactiveFrom) || new Date(0);
            const fromB = normalizeDate(b.inactiveFrom) || new Date(0);

            return fromA - fromB;
        });
    }, [periods, playerMap]);

    // počty pro badge
    const counts = useMemo(() => {
        const all = sortedPeriods.length;
        const current = sortedPeriods.filter((p) => p.isCurrent).length;
        const nonCurrent = all - current;

        return {
            all,
            current,
            nonCurrent,
        };
    }, [sortedPeriods]);

    // filtrování podle aktuálního filtru
    const filteredPeriods = useMemo(() => {
        switch (filter) {
            case FILTERS.CURRENT:
                return sortedPeriods.filter((p) => p.isCurrent);
            case FILTERS.NON_CURRENT:
                return sortedPeriods.filter((p) => !p.isCurrent);
            case FILTERS.ALL:
            default:
                return sortedPeriods;
        }
    }, [sortedPeriods, filter]);

    const loading = inactivityLoading || playersLoading;
    const error = inactivityError || playersError;

    if (loading) {
        return <p>Načítám informace o neaktivitě hráčů…</p>;
    }

    if (error) {
        return (
            <div className="alert alert-danger">
                {error}
                <div className="mt-2">
                    <button
                        type="button"
                        className="btn btn-sm btn-outline-light"
                        onClick={handleReloadAll}
                    >
                        Zkusit znovu načíst
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="container py-3">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h1 className="h3 mb-0">Neaktivity hráčů</h1>
                <button
                    type="button"
                    className="btn btn-outline-secondary btn-sm"
                    onClick={handleReloadAll}
                >
                    Znovu načíst
                </button>
            </div>

            <p className="text-muted mb-3">
                Celkový počet období neaktivity:{" "}
                <strong>{sortedPeriods.length}</strong>
            </p>

            {/* Filtrovací tlačítka */}
            <div className="d-flex justify-content-center mb-4">
                <div
                    className="btn-group"
                    role="group"
                    aria-label="Filtr období neaktivity"
                >
                    <button
                        type="button"
                        className={
                            filter === FILTERS.ALL
                                ? "btn btn-primary"
                                : "btn btn-outline-primary"
                        }
                        onClick={() => setFilter(FILTERS.ALL)}
                    >
                        Všechna{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.all}
                        </span>
                    </button>

                    <button
                        type="button"
                        className={
                            filter === FILTERS.CURRENT
                                ? "btn btn-primary"
                                : "btn btn-outline-primary"
                        }
                        onClick={() => setFilter(FILTERS.CURRENT)}
                    >
                        Aktuálně neaktivní{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.current}
                        </span>
                    </button>

                    <button
                        type="button"
                        className={
                            filter === FILTERS.NON_CURRENT
                                ? "btn btn-primary"
                                : "btn btn-outline-primary"
                        }
                        onClick={() => setFilter(FILTERS.NON_CURRENT)}
                    >
                        Neaktuální{" "}
                        <span className="badge bg-light text-dark ms-1">
                            {counts.nonCurrent}
                        </span>
                    </button>
                </div>
            </div>

            {filteredPeriods.length === 0 && (
                <p>Pro zvolený filtr nejsou žádná období neaktivity.</p>
            )}

            <div className="d-flex flex-column gap-3">
                {filteredPeriods.map((p) => {
                    const player = playerMap.get(p.playerId);
                    const playerId = p.playerId ?? player?.id ?? "?";
                    const playerName = player?.name || "";
                    const playerSurname = player?.surname || "";

                    return (
                        <div key={p.id} className="card shadow-sm">
                            <div className="card-body">
                                <div className="row align-items-center">
                                    <div className="col-md-3">
                                        <small className="text-muted d-block">
                                            Hráč
                                        </small>
                                        {player ? (
                                            <strong>
                                                {playerSurname.toUpperCase()}{" "}
                                                {playerName}
                                            </strong>
                                        ) : (
                                            <strong>
                                                Neznámý hráč (ID {playerId})
                                            </strong>
                                        )}
                                        <div className="text-muted small">
                                            ID hráče: {playerId}
                                        </div>
                                        {p.isCurrent && (
                                            <span className="badge bg-dark mt-1">
                                                Aktuálně neaktivní
                                            </span>
                                        )}
                                    </div>

                                    <div className="col-md-2">
                                        <small className="text-muted d-block">
                                            Neaktivní od
                                        </small>
                                        {formatDateTime(p.inactiveFrom)}
                                    </div>

                                    <div className="col-md-2">
                                        <small className="text-muted d-block">
                                            Neaktivní do
                                        </small>
                                        {formatDateTime(p.inactiveTo)}
                                    </div>
                                    <div className="col-md-4">
                                        <small className="text-muted d-block">
                                            Důvod neaktivity: 
                                            </small>
                                            {p.inactivityReason}
                                        </div>

                                    <div className="col-md-1 text-md-end mt-3 mt-md-0 d-flex flex-column align-items-md-end align-items-start gap-1">
                                        <div>
                                            <small className="text-muted d-block">
                                                ID období
                                            </small>
                                            {p.id}
                                        </div>
                                        
                                        <button
                                            type="button"
                                            className="btn btn-sm btn-outline-primary"
                                            onClick={() =>
                                                openInactivityModal(player)
                                            }
                                            disabled={!player}
                                        >
                                            Spravovat neaktivitu
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* Modal pro úpravu neaktivity konkrétního hráče */}
            {showInactivityModal && selectedPlayer && (
                <AdminPlayerInactivityModal
                    player={selectedPlayer}
                    onClose={closeInactivityModal}
                    onSaved={() => {
                        // po uložení:
                        // 1) zavřít modal
                        // 2) znovu načíst všechny neaktivity + hráče
                        closeInactivityModal();
                        handleReloadAll();
                    }}
                />
            )}
        </div>
    );
};

export default AdminPlayersInactivityPage;
