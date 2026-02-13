// src/components/MatchActions.jsx
import "./MatchActions.css";

const MatchActions = ({
    playerMatchStatus,
    onRegister,
    onUnregister,
    onExcuse,
    onSubstitute,
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
                // žádná registrace – Budu / Nemohu (omluva) / možná
                return (
                    <div className="d-flex gap-2 justify-content-center">
                        <button
                            type="button"
                            className="btn btn-success action-btn"
                            onClick={onRegister}
                            disabled={disabled}
                        >
                            Přijdu
                        </button>

                        <button
                            type="button"
                            className="btn btn-outline-danger action-btn"
                            onClick={onExcuse}
                            disabled={disabled}
                        >
                            Nemůžu
                        </button>

                        <button
                            type="button"
                            className="btn btn-outline-warning action-btn"
                            onClick={onSubstitute}
                            disabled={disabled}
                        >
                            Možná
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
                            Nakonec nepříjdu
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
                            Tak příjdu
                        </button>
                    </div>
                );

            case "SUBSTITUTE":
            // možná - náhradník – může se znova přihlásit
                return (
                    <div className="d-flex justify-content-center">
                        <button
                            type="button"
                            className="btn btn-success action-btn"
                            onClick={onRegister}
                            disabled={disabled}
                        >
                            Tak příjdu
                        </button>

                        <button
                            type="button"
                            className="btn btn-danger action-btn"
                            onClick={onExcuse}
                            disabled={disabled}
                        >
                            Nemůžu
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
