// src/components/MatchActions.jsx

const MatchActions = ({ status, onRegister, onUnregister, onExcuse, disabled }) => {
    console.log("MatchActions status:", status, "disabled:", disabled);

    const renderButtons = () => {
        switch (status) {
            case "NO_RESPONSE":
                // žádná registrace – Budu / Nebudu (omluva)
                return (
                    <>
                        <button
                            type="button"
                            className="btn btn-success"
                            onClick={onRegister}
                            disabled={disabled}
                        >
                            Budu
                        </button>

                        <button
                            type="button"
                            className="btn btn-outline-warning"
                            onClick={onExcuse}
                            disabled={disabled}
                        >
                            Nebudu
                        </button>
                    </>
                );

            case "REGISTERED":
            case "RESERVED":
                // přihlášen nebo náhradník – může se jen odhlásit
                return (
                    <button
                        type="button"
                        className="btn btn-outline-danger"
                        onClick={onUnregister}
                        disabled={disabled}
                    >
                        Nakonec nebudu
                    </button>
                );

            case "EXCUSED":
            case "UNREGISTERED":
                // omluven / odhlášen – může se znova přihlásit
                return (
                    <button
                        type="button"
                        className="btn btn-success"
                        onClick={onRegister}
                        disabled={disabled}
                    >
                        Nakonec budu
                    </button>
                );

            case "NO_EXCUSED":
                // neomluven – stav uzavřen
                return null;

            default:
                console.warn("Neznámý status v MatchActions:", status);
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
