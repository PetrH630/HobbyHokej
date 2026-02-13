import { useNavigate } from "react-router-dom";

const BackButton = ({ label = "Zpět" }) => {
    const navigate = useNavigate();

    return (
        <div className="text-center mt-3">
            <button
                type="button"
                className="btn btn-outline-primary"
                
                onClick={() => navigate(-1)}
            >
                ← {label}
            </button>
        </div>
    );
};

export default BackButton;