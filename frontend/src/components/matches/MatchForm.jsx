// src/components/matches/MatchForm.jsx
import DateTimePicker from "../forms/DateTimePicker";

const MatchForm = ({ values, onChange, errors = {} }) => {
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        onChange({ [name]: value });
    };

    const dateTimeClass = "form-control" + (errors.dateTime ? " is-invalid" : "");
    const locationClass = "form-control" + (errors.location ? " is-invalid" : "");
    const descriptionClass = "form-control" + (errors.description ? " is-invalid" : "");
    const maxPlayersClass = "form-control" + (errors.maxPlayers ? " is-invalid" : "");
    const priceClass = "form-control" + (errors.price ? " is-invalid" : "");

    return (
        <div>
            <h2 className="h5 mb-3">Zápas</h2>

            {/* DATUM + ČAS */}
            <div className="mb-3">
                <label className="form-label" htmlFor="match-dateTime">
                    Datum a čas zápasu
                </label>

                <DateTimePicker
                    id="match-dateTime"
                    name="dateTime"
                    value={values.dateTime || ""}
                    onChange={(valueString) => onChange({ dateTime: valueString })}
                    placeholder="Vyber datum a čas…"
                    required
                    className={dateTimeClass}
                />

                <div className="form-text">Vyber datum a čas začátku zápasu.</div>

                {errors.dateTime && (
                    <div className="invalid-feedback d-block">
                        {errors.dateTime}
                    </div>
                )}
            </div>

            {/* MÍSTO */}
            <div className="mb-3">
                <label className="form-label" htmlFor="match-location">
                    Místo konání
                </label>
                <input
                    type="text"
                    id="match-location"
                    name="location"
                    className={locationClass}
                    value={values.location || ""}
                    onChange={handleInputChange}
                    placeholder="Např. WERK ARÉNA"
                />
                {errors.location && (
                    <div className="invalid-feedback">
                        {errors.location}
                    </div>
                )}
            </div>

            {/* POPIS */}
            <div className="mb-3">
                <label className="form-label" htmlFor="match-description">
                    Popis (volitelné)
                </label>
                <textarea
                    id="match-description"
                    name="description"
                    className={descriptionClass}
                    value={values.description || ""}
                    onChange={handleInputChange}
                    rows={3}
                    placeholder="Např. přátelské utkání, vezměte si světlé dresy…"
                />
                {errors.description && (
                    <div className="invalid-feedback">
                        {errors.description}
                    </div>
                )}
            </div>

            {/* MAX PLAYERS */}
            <div className="mb-3">
                <label className="form-label" htmlFor="match-maxPlayers">
                    Maximální počet hráčů
                </label>
                <input
                    type="number"
                    id="match-maxPlayers"
                    name="maxPlayers"
                    className={maxPlayersClass}
                    value={values.maxPlayers ?? ""}
                    onChange={handleInputChange}
                    min={1}
                />
                {errors.maxPlayers && (
                    <div className="invalid-feedback">
                        {errors.maxPlayers}
                    </div>
                )}
            </div>

            {/* PRICE */}
            <div className="mb-3">
                <label className="form-label" htmlFor="match-price">
                    Cena (celkem)
                </label>
                <input
                    type="number"
                    id="match-price"
                    name="price"
                    className={priceClass}
                    value={values.price ?? ""}
                    onChange={handleInputChange}
                    min={0}
                />
                {errors.price && (
                    <div className="invalid-feedback">
                        {errors.price}
                    </div>
                )}
            </div>
        </div>
    );
};

export default MatchForm;
