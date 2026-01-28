// src/components/MatchActions.jsx
import "./MatchActions.css";

const MatchActions = ({
    playerMatchStatus,
    onRegister,
    onUnregister,
    onExcuse,
    disabled,
}) => {
    console.log(
        "MatchActions status:",
        playerMatchStatus,
        "disabled:",
        disabled
    );

    const renderButtons = () => {
        switch (playerMatchStatus) {
            case "NO_RESPONSE":
                // žádná registrace – Budu / Nemohu (omluva)
                return (
                    <div className="d-flex gap-2 justify-content-center">
                        <button
                            type="button"
                            className="btn btn-success action-btn"
                            onClick={onRegister}
                            disabled={disabled}
                        >
                            Budu
                        </button>

                        <button
                            type="button"
                            className="btn btn-outline-danger action-btn"
                            onClick={onExcuse}
                            disabled={disabled}
                        >
                            Nemohu
                        </button>
                    </div>
                );

            case "REGISTERED":
            case "RESERVED":
                // přihlášen nebo náhradník – může se jen odhlásit
                return (
                    <div className="d-flex justify-content-center">
                        <button
                            type="button"
                            className="btn btn-outline-danger action-btn"
                            onClick={onUnregister}
                            disabled={disabled}
                        >
                            Nakonec nebudu
                        </button>
                    </div>
                );

            case "EXCUSED":
            case "UNREGISTERED":
                // omluven / odhlášen – může se znova přihlásit
                return (
                    <div className="d-flex justify-content-center">
                        <button
                            type="button"
                            className="btn btn-success action-btn"
                            onClick={onRegister}
                            disabled={disabled}
                        >
                            Nakonec budu
                        </button>
                    </div>
                );

            case "NO_EXCUSED":
                // neomluven – stav uzavřen
                return null;

            default:
                console.warn(
                    "Neznámý status v MatchActions:",
                    playerMatchStatus
                );
                return null;
        }
    };

    return (
        <div className="d-flex flex-wrap gap-2 justify-content-center mb-4">
            {renderButtons()}
        </div>
    );
};

export default MatchActions;
