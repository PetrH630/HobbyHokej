// src/pages/ForgottenPasswordRequestPage.jsx

import { useState } from "react";
import { requestForgottenPassword } from "../api/authApi";

const ForgottenPasswordRequestPage = () => {
    const [email, setEmail] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [successMessage, setSuccessMessage] = useState("");
    const [errorMessage, setErrorMessage] = useState("");

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        setSuccessMessage("");
        setErrorMessage("");

        try {
            await requestForgottenPassword(email);
            setSuccessMessage(
                "Pokud existuje účet s tímto e-mailem, byl odeslán odkaz pro reset hesla."
            );
        } catch (err) {
            console.error(err);
            setErrorMessage("Nastala chyba při odesílání požadavku. Zkuste to prosím znovu.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="container mt-4">
            <h1>Zapomenuté heslo</h1>
            <p className="text-muted">
                Zadejte e-mail, který používáte k přihlášení. Pošleme vám odkaz pro nastavení nového hesla.
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

                <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={submitting}
                >
                    {submitting ? "Odesílám..." : "Odeslat odkaz pro reset hesla"}
                </button>
            </form>
        </div>
    );
};

export default ForgottenPasswordRequestPage;
