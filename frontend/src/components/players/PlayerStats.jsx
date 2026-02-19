// src/components/players/PlayerStats.jsx
import React, { useMemo } from "react";
import SeasonSelect from "../seasons/seasonSelect";
import PlayerStatsCharts from "./PlayerStatsCharts";

const PlayerStats = ({ stats, loading, error, onReload, onSeasonChange }) => {
    const safeNum = (v) => (Number.isFinite(Number(v)) ? Number(v) : 0);

    const teamLabel = (team) => {
        if (team === "LIGHT") return "Light";
        if (team === "DARK") return "Dark";
        return "—";
    };

    const totals = useMemo(() => {
        const allMatchesInSeason = safeNum(stats?.allMatchesInSeason);
        const allMatchesInSeasonForPlayer = safeNum(stats?.allMatchesInSeasonForPlayer);

        const registered = safeNum(stats?.registered);
        const unregistered = safeNum(stats?.unregistered);
        const excused = safeNum(stats?.excused);
        const substituted = safeNum(stats?.substituted);
        const reserved = safeNum(stats?.reserved);
        const noResponse = safeNum(stats?.noResponse);
        const noExcused = safeNum(stats?.noExcused);

        const responded = registered + unregistered + excused + substituted + reserved;
        const denominator = allMatchesInSeasonForPlayer > 0 ? allMatchesInSeasonForPlayer : 0;

        const responseRate = denominator > 0 ? Math.round((responded / denominator) * 100) : 0;
        const noResponseRate = denominator > 0 ? Math.round((noResponse / denominator) * 100) : 0;

        // registrace (REGISTERED) podle týmu
        const registeredByTeam = stats?.registeredByTeam ?? {};
        const registeredLight = safeNum(registeredByTeam?.LIGHT);
        const registeredDark = safeNum(registeredByTeam?.DARK);

        // domácí tým
        const homeTeam = stats?.homeTeam ?? null;

        return {
            allMatchesInSeason,
            allMatchesInSeasonForPlayer,
            registered,
            unregistered,
            excused,
            substituted,
            reserved,
            noResponse,
            noExcused,
            responded,
            responseRate,
            noResponseRate,
            denominator,

            homeTeam,
            registeredLight,
            registeredDark,
        };
    }, [stats]);

    const StatCard = ({ label, value, helper }) => (
        <div className="col-12 col-md-6 col-xl-3">
            <div className="card h-100 shadow-sm">
                <div className="card-body">
                    <div className="text-muted small">{label}</div>
                    <div className="display-6 mb-0">{loading ? "…" : value}</div>
                    {helper ? <div className="text-muted small mt-2">{helper}</div> : null}
                </div>
            </div>
        </div>
    );

    const Row = ({ label, value, total, badge }) => {
        const v = safeNum(value);
        const t = safeNum(total);
        const pct = t > 0 ? Math.round((v / t) * 100) : 0;

        return (
            <div className="mb-3">
                <div className="d-flex justify-content-between align-items-center mb-1">
                    <div className="d-flex align-items-center gap-2">
                        <span className="fw-semibold">{label}</span>
                        {badge ? <span className={`badge ${badge}`}> </span> : null}
                    </div>
                    <div className="text-muted small">
                        {loading ? "…" : `${v} / ${t}`}{" "}
                        <span className="ms-2">{loading ? "" : `${pct}%`}</span>
                    </div>
                </div>
                <div className="progress" style={{ height: 8 }}>
                    <div
                        className="progress-bar"
                        role="progressbar"
                        style={{ width: `${loading ? 0 : pct}%` }}
                        aria-valuenow={pct}
                        aria-valuemin="0"
                        aria-valuemax="100"
                    />
                </div>
            </div>
        );
    };

    return (
        <div className="card shadow-sm">
            <div className="card-header bg-white d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
                <div className="fw-semibold">
                    Statistiky hráče - pouze pro již proběhlé zápasy
                </div>

                <div className="d-flex flex-wrap gap-2 align-items-center w-100 w-md-auto justify-content-start justify-content-md-end">
                    <SeasonSelect
                        onSeasonChange={async (id) => {
                            await onSeasonChange?.(id);
                            await onReload?.();
                        }}
                    />

                    {onReload ? (
                        <button
                            type="button"
                            className="btn btn-sm btn-outline-secondary"
                            onClick={onReload}
                            disabled={loading}
                            title="Znovu načíst statistiky"
                        >
                            Obnovit
                        </button>
                    ) : null}
                </div>
            </div>


            <div className="card-body">
                {error ? (
                    <div className="alert alert-danger mb-3" role="alert">
                        {error}
                    </div>
                ) : null}

                {!loading && !error && !stats ? (
                    <div className="text-muted">Statistiky nejsou k dispozici.</div>
                ) : null}

                {/* Metriky */}
                <div className="row g-3 mb-3">
                    <StatCard
                        label="Zápasy v sezóně"
                        value={totals.allMatchesInSeason}
                        helper="Celkový počet v aktuální sezóně"
                    />
                    <StatCard
                        label="Zápasy pro hráče"
                        value={totals.allMatchesInSeasonForPlayer}
                        helper="Dostupné pro aktuálního hráče"
                    />
                    <StatCard
                        label="Míra reakcí"
                        value={loading ? "…" : `${totals.responseRate}%`}
                        helper="Reakce (ano/ne/omluva/…) vs. dostupné zápasy"
                    />
                    <StatCard
                        label="Bez reakce"
                        value={loading ? "…" : `${totals.noResponseRate}%`}
                        helper="Podíl zápasů bez odpovědi"
                    />
                </div>

                {/* ✅ Grafy: donut + bar */}
                <PlayerStatsCharts totals={totals} loading={loading} />

                {/* Týmové info */}
                <div className="alert alert-light border mb-3">
                    <div className="d-flex flex-column flex-md-row justify-content-between gap-2">
                        <div>
                            <div className="fw-semibold">Domácí tým</div>
                            <div className="text-muted small">
                                Tým uložený u hráče v profilu.
                            </div>
                        </div>
                        <div className="fs-5 fw-semibold">
                            {loading ? "…" : teamLabel(totals.homeTeam)}
                        </div>
                    </div>

                    <hr className="my-3" />

                    <div className="d-flex flex-column flex-md-row justify-content-between gap-2">
                        <div>
                            <div className="fw-semibold">Za který tým hrál v sezóně</div>
                            <div className="text-muted small">
                                Počet účastí na odehraných zápasech podle týmu.
                            </div>
                        </div>
                        <div className="fs-6">
                            {loading ? (
                                "…"
                            ) : (
                                <>
                                    <span className="fw-semibold">Light:</span> {totals.registeredLight}
                                    <span className="mx-2">/</span>
                                    <span className="fw-semibold">Dark:</span> {totals.registeredDark}
                                </>
                            )}
                        </div>
                    </div>
                </div>

                <hr />

                {/* Detail */}
                <div className="fw-semibold mb-2">Historie zápasu hráče</div>

                <Row label="Byl" value={totals.registered} total={totals.denominator} />
                <Row label="Nebyl" value={totals.unregistered} total={totals.denominator} />
                <Row label="Nemohl" value={totals.excused} total={totals.denominator} />
                <Row label="Dal možná" value={totals.substituted} total={totals.denominator} />
                <Row label="Čekal na místo" value={totals.reserved} total={totals.denominator} />
                <Row label="Nereagoval" value={totals.noResponse} total={totals.denominator} />

                <div className="alert alert-light border mt-3 mb-0">
                    <div className="d-flex justify-content-between align-items-center">
                        <div>
                            <div className="fw-semibold">Neomluvená neúčast</div>
                            <div className="text-muted small">
                                Zápasy, kde hráč nenastoupil a nebyl omluven.
                            </div>
                        </div>
                        <div className="fs-4">{loading ? "…" : totals.noExcused}</div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PlayerStats;
