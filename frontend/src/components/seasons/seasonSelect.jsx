// src/components/season/SeasonSelect.jsx
import { useSeason } from "../../hooks/useSeason";

const SeasonSelect = () => {
    const {
        seasons,
        currentSeasonId,
        changeSeason,
        loading,
        error,
    } = useSeason();

    if (error) {
        return (
            <div className="text-danger small">
                {error}
            </div>
        );
    }

    // když žádné sezóny nejsou, prostě nic nezobrazuj
    if (!seasons || seasons.length === 0) {
        return null;
    }

    return (
        <div className="d-flex align-items-center gap-2">
            <label
                htmlFor="seasonSelect"
                className="form-label mb-0 me-2"
                style={{ fontWeight: 500 }}
            >
                Sezóna:
            </label>

            <select
                id="seasonSelect"
                className="form-select form-select-sm w-auto"
                value={currentSeasonId ?? ""}
                onChange={(e) => changeSeason(Number(e.target.value))}
                disabled={loading}
            >
                {seasons.map((s) => (
                    <option key={s.id} value={s.id}>
                        {s.name}
                    </option>
                ))}
            </select>

            {loading && (
                <span className="text-muted small ms-2">
                    Ukládám…
                </span>
            )}
        </div>
    );
};

export default SeasonSelect;
