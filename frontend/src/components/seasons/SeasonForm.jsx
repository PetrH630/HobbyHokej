// src/components/seasons/SeasonForm.jsx
import DatePicker from "../forms/DatePicker";

const SeasonForm = ({ values, onChange, errors = {} }) => {
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        onChange({ [name]: value });
    };

    // kompatibilní handler pro DateTimePicker:
    // - buď zavolá onChange(event)
    // - nebo onChange(valueString)
    const handleDateChange = (name) => (valueOrEvent) => {
        const value =
            valueOrEvent?.target?.value !== undefined
                ? valueOrEvent.target.value
                : valueOrEvent;

        onChange({ [name]: value });
    };

    const nameClass = "form-control" + (errors.name ? " is-invalid" : "");
    const startDateClass =
        "form-control" + (errors.startDate ? " is-invalid" : "");
    const endDateClass =
        "form-control" + (errors.endDate ? " is-invalid" : "");

    return (
        <div>
            <h2 className="h5 mb-3">Sezóna</h2>

            <div className="mb-3">
                <label className="form-label" htmlFor="season-name">
                    Název sezóny
                </label>
                <input
                    type="text"
                    id="season-name"
                    name="name"
                    className={nameClass}
                    value={values.name || ""}
                    onChange={handleInputChange}
                    placeholder="např. 2025/2026"
                />
                {errors.name && (
                    <div className="invalid-feedback">{errors.name}</div>
                )}
            </div>

            <div className="row">
                <div className="col-md-6 mb-3">
                    <label className="form-label" htmlFor="season-startDate">
                        Začátek sezóny
                    </label>

                    <DatePicker
                        id="season-startDate"
                        name="startDate"
                        value={values.startDate || ""}
                        onChange={handleDateChange("startDate")}
                        className={startDateClass}
                    />

                    {errors.startDate && (
                        <div className="invalid-feedback d-block">
                            {errors.startDate}
                        </div>
                    )}
                </div>

                <div className="col-md-6 mb-3">
                    <label className="form-label" htmlFor="season-endDate">
                        Konec sezóny
                    </label>

                    <DatePicker
                        id="season-endDate"
                        name="endDate"
                        value={values.endDate || ""}
                        onChange={handleDateChange("endDate")}
                        className={endDateClass}
                    />

                    {errors.endDate && (
                        <div className="invalid-feedback d-block">
                            {errors.endDate}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default SeasonForm;
