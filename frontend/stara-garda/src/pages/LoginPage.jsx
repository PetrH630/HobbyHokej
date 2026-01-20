import { useState, useEffect } from "react";
import { login, checkAuth } from "../api/auth";
import { useNavigate } from "react-router-dom";

const LoginPage = () => {
    const navigate = useNavigate();
    const [email, setEmail] = useState(""); // změněno z username na email
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const verifyAuth = async () => {
            const isAuth = await checkAuth();
            if (isAuth) navigate("/");
        };
        verifyAuth();
    }, [navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            await login(email, password); // 🔹 JSON login
            navigate("/"); // přesměrování po úspěšném loginu
        } catch (err) {
            const msg = err?.response?.data?.message || "Neplatné přihlašovací údaje";
            setError(msg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container mt-4">
            <div className="row justify-content-center">
                <div className="col-md-6 col-lg-5">
                    <div className="card shadow p-4">
                        <h3 className="text-center mb-4">Přihlášení</h3>

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

                            <div className="mb-3">
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

                            {/* ✅ NOVÉ TLAČÍTKO PRO REGISTRACI */}
                            <button
                                type="button"
                                className="btn btn-outline-secondary w-100"
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
