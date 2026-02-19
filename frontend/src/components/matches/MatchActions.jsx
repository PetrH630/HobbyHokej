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
    const renderButtons = () => {
        switch (playerMatchStatus) {
            case "NO_RESPONSE":
                return (
                    <>
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
                    </>
                );

            case "REGISTERED":
            case "RESERVED":
                return (
                    <button
                        type="button"
                        className="btn btn-outline-danger action-btn"
                        onClick={onUnregister}
                        disabled={disabled}
                    >
                        Nakonec nepříjdu
                    </button>
                );

            case "EXCUSED":
            case "UNREGISTERED":
                return (
                    <button
                        type="button"
                        className="btn btn-success action-btn"
                        onClick={onRegister}
                        disabled={disabled}
                    >
                        Tak příjdu
                    </button>
                );

            case "SUBSTITUTE":
                return (
                    <>
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
                    </>
                );

            case "NO_EXCUSED":
                return null;

            default:
                return null;
        }
    };

    const buttons = renderButtons();
    if (!buttons) return null;

    return (
        <div className="match-actions">
            {buttons}
        </div>
    );
};

export default MatchActions;
