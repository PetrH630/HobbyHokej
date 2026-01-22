import { useState } from "react";

const CreatePlayerForm = ({ onSubmit, onCancel, submitting }) => {
    const [name, setName] = useState("");
    const [surname, setSurname] = useState("");
    const [nickName, setNickName] = useState("");
    const [phoneNumber, setPhoneNumber] = useState("");
    const [team, setTeam] = useState("DARK");

    const handleSubmit = (e) => {
        e.preventDefault();

        onSubmit({
            name,
            surname,
            nickName: nickName || null,
            phoneNumber: phoneNumber || null,
            team,
        });
    };

    return (
        <form onSubmit={handleSubmit}>
            <div className="mb-3">
                <label className="form-label">Jméno *</label>
                <input
                    type="text"
                    className="form-control"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    required
                    minLength={2}
                />
            </div>

            <div className="mb-3">
                <label className="form-label">Příjmení *</label>
                <input
                    type="text"
                    className="form-control"
                    value={surname}
                    onChange={(e) => setSurname(e.target.value)}
                    required
                    minLength={2}
                />
            </div>

            <div className="mb-3">
                <label className="form-label">
                    Přezdívka (nepovinné)
                </label>
                <input
                    type="text"
                    className="form-control"
                    value={nickName}
                    onChange={(e) => setNickName(e.target.value)}
                />
            </div>

            <div className="mb-3">
                <label className="form-label">
                    Telefon (nepovinné)
                </label>
                <input
                    type="tel"
                    className="form-control"
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                />
            </div>

            <div className="mb-3">
                <label className="form-label">Tým</label>
                <select
                    className="form-select"
                    value={team}
                    onChange={(e) => setTeam(e.target.value)}
                >
                    <option value="DARK">DARK</option>
                    <option value="LIGHT">LIGHT</option>
                </select>
            </div>

            <div className="d-flex justify-content-between mt-4">
                <button
                    type="button"
                    className="btn btn-outline-secondary"
                    onClick={onCancel}
                    disabled={submitting}
                >
                    Zrušit
                </button>

                <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={submitting}
                >
                    {submitting ? "Ukládám…" : "Vytvořit hráče"}
                </button>
            </div>
        </form>
    );
};

export default CreatePlayerForm;
