import React, { useMemo } from "react";
import SeasonSelect from "../seasons/seasonSelect";
import PlayerStatsCharts from "./PlayerStatsCharts";
import {
    TeamDarkIcon,
    TeamLightIcon,
} from "../../icons";

/**
 * PlayerStats
 *
 * React komponenta zobrazující statistiky přihlášeného hráče pro vybranou sezónu.
 * Data pochází z endpointu /api/players/me/stats.
 */
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

        const registeredByTeam = stats?.registeredByTeam ?? {};
        const registeredDark = safeNum(registeredByTeam.DARK);
        const registeredLight = safeNum(registeredByTeam.LIGHT);

        const homeTeam = stats?.homeTeam ?? null;

        const registeredMatchResults = Array.isArray(stats?.registeredMatchResults)
            ? stats.registeredMatchResults
            : [];

        let darkWins = 0;
        let darkLosses = 0;
        let darkDraws = 0;

        let lightWins = 0;
        let lightLosses = 0;
        let lightDraws = 0;

        let sumScoreDark = 0;
        let sumScoreLight = 0;

        registeredMatchResults.forEach((m) => {
            const team = m?.playerTeam;
            const result = m?.result;

            sumScoreDark += safeNum(m?.scoreDark);
            sumScoreLight += safeNum(m?.scoreLight);

            if (team === "DARK") {
                if (result === "DARK_WIN") darkWins += 1;
                else if (result === "LIGHT_WIN") darkLosses += 1;
                else if (result === "DRAW") darkDraws += 1;
            }

            if (team === "LIGHT") {
                if (result === "LIGHT_WIN") lightWins += 1;
                else if (result === "DARK_WIN") lightLosses += 1;
                else if (result === "DRAW") lightDraws += 1;
            }
        });

        const playedWithResult =
            darkWins +
            darkLosses +
            darkDraws +
            lightWins +
            lightLosses +
            lightDraws;

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
            denominator,
            responseRate,
            noResponseRate,

            homeTeam,
            registeredDark,
            registeredLight,

            darkWins,
            darkLosses,
            darkDraws,

            lightWins,
            lightLosses,
            lightDraws,

            playedWithResult,

            sumScoreDark,
            sumScoreLight,
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

    const Row = ({ label, value, total }) => {
        const v = safeNum(value);
        const t = safeNum(total);
        const pct = t > 0 ? Math.round((v / t) * 100) : 0;

        return (
            <div className="mb-3">
                <div className="d-flex justify-content-between align-items-center mb-1">
                    <span className="fw-semibold">{label}</span>
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

                <div className="d-flex flex-wrap gap-2 align-items-center">
                    <SeasonSelect
                        onSeasonChange={async (id) => {
                            await onSeasonChange?.(id);
                            await onReload?.();
                        }}
                    />

                    {onReload && (
                        <button
                            type="button"
                            className="btn btn-sm btn-outline-secondary"
                            onClick={onReload}
                            disabled={loading}
                        >
                            Obnovit
                        </button>
                    )}
                </div>
            </div>

            <div className="card-body">
                {error && (
                    <div className="alert alert-danger mb-3" role="alert">
                        {error}
                    </div>
                )}

                {/* základní metriky */}
                <div className="row g-3 mb-3">
                    <StatCard label="Zápasy v sezóně" value={totals.allMatchesInSeason} />
                    <StatCard label="Zápasy pro hráče" value={totals.allMatchesInSeasonForPlayer} />
                    <StatCard label="Míra reakcí" value={`${totals.responseRate}%`} />
                    <StatCard label="Bez reakce" value={`${totals.noResponseRate}%`} />

                    {/* výsledky */}
                    <div className="col-12">
                        <div className="card border shadow-sm">
                            <div className="card-body">

                                <div className="fw-semibold mb-2">Hrál za <TeamDarkIcon className="match-reg-team-icon-dark" /></div>
                                <div className="mb-2">
                                    Výhry: {totals.darkWins} | Prohry: {totals.darkLosses} | Remízy: {totals.darkDraws}
                                </div>

                                <div className="fw-semibold mb-2">Hrál za <TeamLightIcon className="match-reg-team-icon-light" /></div>
                                <div className="mb-2">
                                    Výhry: {totals.lightWins} | Prohry: {totals.lightLosses} | Remízy: {totals.lightDraws}
                                </div>

                                <div className="fw-semibold mt-3">
                                    Score: <TeamDarkIcon className="match-reg-team-icon-dark" /> {totals.sumScoreDark} : {totals.sumScoreLight} <TeamLightIcon className="match-reg-team-icon-light" />
                                </div>

                                <div className="text-muted small mt-1">
                                    Započítáno: {totals.playedWithResult}
                                </div>

                            </div>
                        </div>
                    </div>
                </div>

                <PlayerStatsCharts totals={totals} loading={loading} />

                <div className="alert alert-light border mb-3">
                    <div className="d-flex justify-content-between align-items-center">
                        <div>
                            <div className="fw-semibold">Domácí tým</div>
                            <div className="text-muted small">Tým uložený u hráče</div>
                        </div>

                        <div className="fs-5 fw-semibold d-flex align-items-center gap-2">
                            {totals.homeTeam === "DARK" && (
                                <TeamDarkIcon className="match-reg-team-icon-dark" />
                            )}
                            {totals.homeTeam === "LIGHT" && (
                                <TeamLightIcon className="match-reg-team-icon-light" />
                            )}

                            {teamLabel(totals.homeTeam)}
                        </div>
                    </div>

                    <hr />

                    <div className="d-flex justify-content-between align-items-center">
                        <div>
                            <div className="fw-semibold">Za který tým hrál</div>
                        </div>

                        <div className="d-flex align-items-center gap-3">
                            <span className="d-flex align-items-center gap-1">
                                <TeamDarkIcon className="match-reg-team-icon-dark" />
                                {totals.registeredDark} {" - "}
                            </span>
                            
                            <span className="d-flex align-items-center gap-1">
                                <TeamLightIcon className="match-reg-team-icon-light" />
                                {totals.registeredLight}
                            </span>

                            
                        </div>
                    </div>
                </div>

                <hr />

                <div className="fw-semibold mb-2">Historie zápasu hráče</div>

                <Row label="Byl" value={totals.registered} total={totals.denominator} />
                <Row label="Nebyl" value={totals.unregistered} total={totals.denominator} />
                <Row label="Nemohl" value={totals.excused} total={totals.denominator} />
                <Row label="Dal možná" value={totals.substituted} total={totals.denominator} />
                <Row label="Čekal na místo" value={totals.reserved} total={totals.denominator} />
                <Row label="Nereagoval" value={totals.noResponse} total={totals.denominator} />

                <div className="alert alert-light border mt-3 mb-0">
                    <div className="d-flex justify-content-between">
                        <div>
                            <div className="fw-semibold">Neomluvená neúčast</div>
                        </div>
                        <div className="fs-4">{totals.noExcused}</div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PlayerStats;