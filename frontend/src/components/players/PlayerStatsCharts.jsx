// src/components/players/PlayerStatsCharts.jsx
import React, { useMemo } from "react";
import {
    ResponsiveContainer,
    PieChart,
    Pie,
    Cell,
    Tooltip,
    Legend,
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
} from "recharts";

/**
 * Barvy dle statusů (sjednocené s UI kartami).
 */
const STATUS_COLORS = {
    registered: "#29d158",    // match-registered
    unregistered: "#ffe066",  // match-unregistered (border)
    excused: "#27c3f3",       // match-excused (vychází z modré)
    substituted: "#ffd43b",   // "možná" – žlutá (nemáš v kartě explicitně, dává smysl)
    reserved: "#0d6efd",      // reserved (ikonka je modrá)
    noResponse: "#adb5bd",    // no-response šedá
    noExcused: "#dc3545",     // no-excused červená
};

const PlayerStatsCharts = ({ totals, loading }) => {
    const safeNum = (v) => (Number.isFinite(Number(v)) ? Number(v) : 0);

    const series = useMemo(() => {
        const items = [
            { key: "registered", label: "Byl", value: safeNum(totals?.registered) },
            { key: "unregistered", label: "Zrušil", value: safeNum(totals?.unregistered) },
            { key: "excused", label: "Omluvil se", value: safeNum(totals?.excused) },
            { key: "substituted", label: "Dal možná", value: safeNum(totals?.substituted) },
            { key: "reserved", label: "Čekal místo", value: safeNum(totals?.reserved) },
            { key: "noResponse", label: "Nereagoval", value: safeNum(totals?.noResponse) },
            // pokud chceš i do grafu:
            // { key: "noExcused", label: "Neomluven", value: safeNum(totals?.noExcused) },
        ];

        // do grafů necpeme nuly (čistší grafy)
        return items
            .filter((i) => i.value > 0)
            .map((i) => ({
                ...i,
                color: STATUS_COLORS[i.key] || "#0d6efd",
            }));
    }, [totals]);

    const totalSum = useMemo(
        () => series.reduce((acc, i) => acc + i.value, 0),
        [series]
    );

    const TooltipBox = ({ active, payload }) => {
        if (!active || !payload || payload.length === 0) return null;

        // u Pie i u Bar tohle typicky sedí
        const p = payload[0]?.payload;
        if (!p) return null;

        const pct = totalSum > 0 ? Math.round((p.value / totalSum) * 100) : 0;

        return (
            <div className="p-2 bg-white border rounded shadow-sm">
                <div className="fw-semibold">{p.label}</div>
                <div className="text-muted small">
                    {p.value} {totalSum > 0 ? `(${pct}%)` : ""}
                </div>
            </div>
        );
    };

    if (loading) {
        return (
            <div className="row g-3 mb-3">
                <div className="col-12 col-lg-6">
                    <div className="card shadow-sm h-100">
                        <div className="card-header bg-white fw-semibold">
                            Moje registrace na zápasy
                        </div>
                        <div className="card-body text-muted">Načítám graf…</div>
                    </div>
                </div>

                <div className="col-12 col-lg-6">
                    <div className="card shadow-sm h-100">
                        <div className="card-header bg-white fw-semibold">
                            Porovnání statusů
                        </div>
                        <div className="card-body text-muted">Načítám graf…</div>
                    </div>
                </div>
            </div>
        );
    }

    if (!series || series.length === 0) {
        return (
            <div className="alert alert-light border mb-3">
                Zatím není co vykreslit do grafu (všechny hodnoty jsou 0).
            </div>
        );
    }

    return (
        <div className="row g-3 mb-3">
            {/* Donut */}
            <div className="col-12 col-lg-6">
                <div className="card shadow-sm h-100">
                    <div className="card-header bg-white fw-semibold">
                        Moje registrace na zápasy
                    </div>

                    <div className="card-body" style={{ height: 320 }}>
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={series}
                                    dataKey="value"
                                    nameKey="label"
                                    innerRadius="55%"
                                    outerRadius="80%"
                                    paddingAngle={2}
                                >
                                    {series.map((s) => (
                                        <Cell key={s.key} fill={s.color} />
                                    ))}
                                </Pie>

                                <Tooltip content={<TooltipBox />} />
                                <Legend verticalAlign="bottom" height={36} />
                            </PieChart>
                        </ResponsiveContainer>

                        
                    </div>
                </div>
            </div>

            {/* Bar */}
            <div className="col-12 col-lg-6">
                <div className="card shadow-sm h-100">
                    <div className="card-header bg-white fw-semibold">
                        Porovnání registrací
                    </div>

                    <div className="card-body" style={{ height: 320 }}>
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart
                                data={series}
                                margin={{ top: 10, right: 10, left: 0, bottom: 10 }}
                            >
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="label" />
                                <YAxis allowDecimals={false} />
                                <Tooltip content={<TooltipBox />} />

                                {/* fill je fallback, konkrétní barvy dáváme přes Cell */}
                                <Bar dataKey="value" fill="#0d6efd">
                                    {series.map((s) => (
                                        <Cell key={s.key} fill={s.color} />
                                    ))}
                                </Bar>
                            </BarChart>
                        </ResponsiveContainer>

                       
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PlayerStatsCharts;
