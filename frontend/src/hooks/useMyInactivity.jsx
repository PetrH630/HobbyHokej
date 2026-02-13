// src/hooks/useMyInactivity.js
import { useEffect, useState } from "react";
import { getMyInactivity } from "../api/playerInactivityApi";

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

export const useMyInactivity = () => {
    const [periods, setPeriods] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const load = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await getMyInactivity();

            const now = new Date();

            const withCurrentFlag = (data || []).map((p) => {
                const from = normalizeDate(p.inactiveFrom);
                const to = normalizeDate(p.inactiveTo);

                const isCurrent =
                    from && to ? from <= now && to >= now : false;

                return {
                    ...p,
                    isCurrent,
                };
            });

            // Pro jednoho hráče stačí seřadit podle začátku neaktivity
            withCurrentFlag.sort((a, b) => {
                const fromA = normalizeDate(a.inactiveFrom) || new Date(0);
                const fromB = normalizeDate(b.inactiveFrom) || new Date(0);
                return fromA - fromB;
            });

            setPeriods(withCurrentFlag);
        } catch (err) {
            console.error("Nepodařilo se načíst neaktivity hráče:", err);
            const msg =
                err?.response?.data?.message ||
                "Nepodařilo se načíst období neaktivity.";
            setError(msg);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        load();
    }, []);

    return { periods, loading, error, reload: load };
};
