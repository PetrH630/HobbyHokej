// src/pages/ForgottenPasswordRequestPage.jsx
import { useState } from "react";
import { requestForgottenPassword } from "../api/authApi";
import { tryGetDemoNotifications, tryClearDemoNotifications } from "../api/demoApi";
import DemoNotificationsModal from "../components/demo/DemoNotificationsModal";

const hasAnyDemoItems = (demo) => {
    if (!demo) return false;
    return (demo.emails?.length ?? 0) > 0 || (demo.sms?.length ?? 0) > 0;
};

const ForgottenPasswordRequestPage = () => {
    const [email, setEmail] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [successMessage, setSuccessMessage] = useState("");
    const [errorMessage, setErrorMessage] = useState("");

    // DEMO modal
    const [demoNotifications, setDemoNotifications] = useState(null);
    const [showDemoModal, setShowDemoModal] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        setSuccessMessage("");
        setErrorMessage("");

        try {
            await tryClearDemoNotifications();
            await requestForgottenPassword(email);

            setSuccessMessage(
                "Pokud existuje účet s tímto e-mailem, byl odeslán odkaz pro reset hesla."
            );

            // DEMO: načti a zobraz zachycené notifikace
            const demo = await tryGetDemoNotifications();
            console.log("[ForgottenPasswordRequest] demo:", demo);

            if (hasAnyDemoItems(demo)) {
                setDemoNotifications(demo);
                setShowDemoModal(true);
            }
        } catch (err) {
            console.error(err);
            setErrorMessage(
                "Nastala chyba při odesílání požadavku. Zkuste to prosím znovu."
            );
        } finally {
            setSubmitting(false);
        }
    };

    const handleCloseDemoModal = () => {
        setShowDemoModal(false);
        setDemoNotifications(null);
    };

    return (
        <div className="container mt-4">
            <h1>Zapomenuté heslo</h1>
            <p className="text-muted">
                Zadejte e-mail, který používáte k přihlášení. Pošleme vám odkaz
                pro nastavení nového hesla.
            </p>

            {successMessage && (
                <div className="alert alert-success" role="alert">
                    {successMessage}
                </div>
            )}

            {errorMessage && (
                <div className="alert alert-danger" role="alert">
                    {errorMessage}
                </div>
            )}

            <form onSubmit={handleSubmit} className="mt-3" style={{ maxWidth: "400px" }}>
                <div className="mb-3">
                    <label htmlFor="email" className="form-label">
                        E-mail
                    </label>
                    <input
                        type="email"
                        id="email"
                        className="form-control"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </div>

                <button type="submit" className="btn btn-primary" disabled={submitting}>
                    {submitting ? "Odesílám..." : "Odeslat odkaz pro reset hesla"}
                </button>
            </form>

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

export default ForgottenPasswordRequestPage;
