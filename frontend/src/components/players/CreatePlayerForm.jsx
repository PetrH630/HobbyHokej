import { useState } from "react";
import RoleGuard from "../RoleGuard";
import { PLAYER_POSITION_OPTIONS } from "../../constants/playerPosition";

const CreatePlayerForm = ({ onSubmit, onCancel, submitting }) => {
    const [name, setName] = useState("");
    const [surname, setSurname] = useState("");
    const [nickName, setNickName] = useState("");
    const [phoneNumber, setPhoneNumber] = useState("");
    const [team, setTeam] = useState("DARK");
    const [type, setType] = useState("BASIC");
    const [primaryPosition, setPrimaryPosition] = useState("");
    const [secondaryPosition, setSecondaryPosition] = useState("");

    const handleSubmit = (e) => {
        e.preventDefault();

        onSubmit({
            name,
            surname,
            nickName: nickName || null,
            phoneNumber: phoneNumber || null,
            team,
            type,
            primaryPosition: primaryPosition || null,
            secondaryPosition: secondaryPosition || null,
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

            {/* POST – stejné chování jako v PlayerProfileForm */}
            <div className="mb-3">
                <label className="form-label">Post</label>
                <div className="row g-2">
                    <div className="col-12 col-sm-6">
                        <select
                            className="form-select"
                            value={primaryPosition}
                            onChange={(e) =>
                                setPrimaryPosition(e.target.value)
                            }
                        >
                            <option value="">
                                — Primární post —
                            </option>
                            {PLAYER_POSITION_OPTIONS.map((opt) => (
                                <option key={opt.value} value={opt.value}>
                                    {opt.label}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="col-12 col-sm-6">
                        <select
                            className="form-select"
                            value={secondaryPosition}
                            onChange={(e) =>
                                setSecondaryPosition(e.target.value)
                            }
                        >
                            <option value="">
                                — Sekundární post —
                            </option>
                            {PLAYER_POSITION_OPTIONS.map((opt) => (
                                <option key={opt.value} value={opt.value}>
                                    {opt.label}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>
            </div>

            <RoleGuard roles={["ROLE_ADMIN", "ROLE_MANAGER", "ROLE_PLAYER"]}>
                <div className="mb-3">
                    <label className="form-label">Typ</label>
                    <select
                        className="form-select"
                        value={type}
                        onChange={(e) => setType(e.target.value)}
                    >
                        <option value="BASIC">základní</option>
                        <option value="STANDARD">standardní</option>
                        <option value="VIP">VIP</option>
                    </select>
                    <div className="form-text">
                        Typ hráče - pro zobrazení nadcházejících zápasů
                        (odpovídá enumu Typ).
                    </div>
                </div>
            </RoleGuard>

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