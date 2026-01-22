import React, { useState, useEffect } from "react";
import { useAuth } from "../hooks/useAuth";
import { loginUser } from "../api/authApi";
import { useNavigate } from "react-router-dom";

const LoginPage = () => {
    const navigate = useNavigate();
    const { updateUser } = useAuth();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            await loginUser(email, password);
            await updateUser();               // načte usera do kontextu
            navigate("/Players");             // přesměruj na stránku hráči
        } catch (err) {
            setError(err?.response?.data?.message || "Neplatné přihlášení");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-12 d-flex justify-content-center">
                    <div
                        className="card shadow p-4 mx-auto"
                        style={{ maxWidth: "420px" }}
                    >
                        <h3 className="text-center mb-2">HokejApp</h3>
                        <h5 className="text-center mb-4 text-muted">Přihlášení</h5>   

                        {error && (
                            <div className="alert alert-danger">
                                {error}
                            </div>
                        )}

                        <form onSubmit={handleSubmit}>
                            <div className="mb-3">
                                <label className="form-label">E-mail</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                />
                            </div>

                            <div className="mb-4">
                                <label className="form-label">Heslo</label>
                                <input
                                    type="password"
                                    className="form-control"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                />
                            </div>

                            <button
                                type="submit"
                                className="btn btn-primary w-100 mb-2"
                                disabled={loading}
                            >
                                {loading ? "Přihlašuji…" : "Přihlásit se"}
                            </button>
                            
                            
                            {/*NOVÉ TLAČÍTKO PRO REGISTRACI */}
                            <button
                                type="button"
                                className="btn btn-outline-secondary w-100 mt-3"
                                onClick={() => navigate("/register")}
                            >
                                Registrovat se
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
