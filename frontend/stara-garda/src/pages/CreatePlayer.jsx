import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createPlayer } from "../api/playerApi";
import CreatePlayerForm from "../components/players/CreatePlayerForm";

const CreatePlayer = () => {
    const navigate = useNavigate();
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);

    const handleCreatePlayer = async (data) => {
        setError(null);
        setSubmitting(true);

        try {
            await createPlayer(data);
            navigate("/players");
        } catch (err) {
            const msg =
                err?.response?.data?.message ||
                "Nepodařilo se vytvořit hráče.";
            setError(msg);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="container mt-4">
            <div className="row justify-content-center">
                <div className="col-md-6">

                    <h2 className="mb-4 text-center">
                        Vytvořit nového hráče
                    </h2>

                    {error && (
                        <div className="alert alert-danger">
                            {error}
                        </div>
                    )}

                    <CreatePlayerForm
                        onSubmit={handleCreatePlayer}
                        onCancel={() => navigate("/players")}
                        submitting={submitting}
                    />

                </div>
            </div>
        </div>
    );
};

export default CreatePlayer;
