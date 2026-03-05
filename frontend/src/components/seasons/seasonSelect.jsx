import { useEffect, useState } from "react";
import { useSeason } from "../../hooks/useSeason";

/**
 * SeasonSelect
 *
 * React komponenta používaná ve frontend aplikaci.
 *
 * Props:
 * @param {Object} props.onSeasonChange vstupní hodnota komponenty.
 */
const SeasonSelect = ({ onSeasonChange }) => {
    const { seasons, currentSeasonId, changeSeason, loading } = useSeason();
    const [value, setValue] = useState(currentSeasonId ?? "");

    useEffect(() => {
        setValue(currentSeasonId ?? "");
    }, [currentSeasonId]);

    
    const handleChange = async (e) => {
        const seasonId = Number(e.target.value);
        setValue(seasonId);

        await changeSeason(seasonId);
        onSeasonChange?.(seasonId);
    };

    return (
        <div className="d-inline-flex align-items-center gap-2">
            <span className="fw-semibold">Sezóna:</span>

            <select
                className="form-select form-select-sm w-auto"
                style={{ minWidth: "max-content" }}
                value={value}
                onChange={handleChange}
                disabled={loading || seasons.length === 0}
            >
                {seasons.map((s) => (
                    <option key={s.id} value={s.id}>
                        {s.name}
                    </option>
                ))}
            </select>
        </div>
    );
};

export default SeasonSelect;
