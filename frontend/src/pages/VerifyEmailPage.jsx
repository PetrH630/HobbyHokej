// src/pages/VerifyEmailPage.jsx

import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { verifyEmail } from "../api/authApi";
import { tryGetDemoNotifications, tryClearDemoNotifications } from "../api/demoApi";
import DemoNotificationsModal from "../components/demo/DemoNotificationsModal";

const hasAnyDemoItems = (demo) => {
    if (!demo) return false;
    return (demo.emails?.length ?? 0) > 0 || (demo.sms?.length ?? 0) > 0;
};

const VerifyEmailPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const token = searchParams.get("token");

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [successMessage, setSuccessMessage] = useState("");

    const [demoNotifications, setDemoNotifications] = useState(null);
    const [showDemoModal, setShowDemoModal] = useState(false);

    const handleCloseDemoModal = async () => {
        setShowDemoModal(false);
        setDemoNotifications(null);
        await tryClearDemoNotifications(); // pojistka
        navigate("/login");
    };

    useEffect(() => {
        const run = async () => {
            if (!token) {
                setError("Chybí aktivační token.");
                setLoading(false);
                return;
            }

            try {
                // pojistka: kdyby v demo store visely staré zprávy
                await tryClearDemoNotifications();

                const res = await verifyEmail(token);
                setSuccessMessage(res.data || "Účet byl úspěšně aktivován.");

                const demo = await tryGetDemoNotifications(); // GET už stejně dělá getAndClear()
                if (hasAnyDemoItems(demo)) {
                    setDemoNotifications(demo);
                    setShowDemoModal(true);
                    return;
                }

                setTimeout(() => navigate("/login"), 2500);
            } catch (e) {
                console.error(e);
                const msg =
                    e?.response?.data ||
                    e?.response?.data?.message ||
                    "Aktivační odkaz je neplatný nebo expirovaný.";
                setError(msg);
            } finally {
                setLoading(false);
            }
        };

        run();
    }, [token, navigate]);

    return (
        <div className="container mt-4" style={{ maxWidth: 700 }}>
            <h1>Aktivace účtu</h1>

            {loading && <p>Probíhá aktivace…</p>}

            {!loading && error && (
                <div className="alert alert-danger" role="alert">
                    {error}
                </div>
            )}

            {!loading && !error && successMessage && (
                <div className="alert alert-success" role="alert">
                    {successMessage}
                </div>
            )}

            <button
                type="button"
                className="btn btn-outline-secondary"
                onClick={() => navigate("/login")}
            >
                Zpět na přihlášení
            </button>

            {showDemoModal && demoNotifications && (
                <DemoNotificationsModal
                    show={showDemoModal}
                    notifications={demoNotifications}
                    loading={false}
                    error={null}
                    onClose={handleCloseDemoModal}
                />
            )}
        </div>
    );
};

export default VerifyEmailPage;
