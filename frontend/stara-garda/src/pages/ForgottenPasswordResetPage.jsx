// src/pages/ForgottenPasswordResetPage.jsx

import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
    getForgottenPasswordInfo,
    resetForgottenPassword,
} from "../api/authApi";

const ForgottenPasswordResetPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const token = searchParams.get("token");

    const [email, setEmail] = useState("");
    const [loadingInfo, setLoadingInfo] = useState(true);
    const [infoError, setInfoError] = useState("");

    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState("");
    const [submitSuccess, setSubmitSuccess] = useState("");

    // 1) Načtení info podle tokenu (email)
    useEffect(() => {
        const loadInfo = async () => {
            if (!token) {
                setInfoError("Chybí token pro reset hesla.");
                setLoadingInfo(false);
                return;
            }

            try {
                const res = await getForgottenPasswordInfo(token);
                setEmail(res.data.email);
            } catch (err) {
                console.error(err);
                setInfoError("Neplatný nebo expirovaný odkaz pro reset hesla.");
            } finally {
                setLoadingInfo(false);
            }
        };

        loadInfo();
    }, [token]);

    // 2) Odeslání nového hesla
    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitError("");
        setSubmitSuccess("");

        if (newPassword !== confirmPassword) {
            setSubmitError("Hesla se neshodují.");
            return;
        }

        setSubmitting(true);
        try {
            await resetForgottenPassword({
                token,
                newPassword,
                newPasswordConfirm: confirmPassword, // DŮLEŽITÉ: musí sedět na DTO na backendu
            });

            setSubmitSuccess(
                "Heslo bylo úspěšně změněno. Můžete se přihlásit novým heslem."
            );

            // volitelné přesměrování na login po pár sekundách:
            setTimeout(() => {
                navigate("/login");
            }, 3000);
        } catch (err) {
            console.error(err);

            // zkusíme vytáhnout zprávu z backendu, pokud nějaká přišla
            const backendMessage =
                err?.response?.data?.message ||
                err?.response?.data?.error ||
                null;

            setSubmitError(
                backendMessage ||
                "Nastala chyba při změně hesla. Zkuste to prosím znovu."
            );
        } finally {
            setSubmitting(false);
        }
    };

    if (loadingInfo) {
        return (
            <div className="container mt-4">
                <p>Načítám informace...</p>
            </div>
        );
    }

    if (infoError) {
        return (
            <div className="container mt-4">
                <div className="alert alert-danger" role="alert">
                    {infoError}
                </div>
                <button
                    type="button"
                    className="btn btn-outline-primary mt-3"
                    onClick={() => navigate("/forgotten-password")}
                >
                    Zkusit poslat nový odkaz
                </button>
            </div>
        );
    }

    return (
        <div className="container mt-4">
            <h1>Nastavení nového hesla</h1>

            <p className="text-muted">
                Reset hesla pro účet: <strong>{email}</strong>
            </p>

            {submitError && (
                <div className="alert alert-danger" role="alert">
                    {submitError}
                </div>
            )}

            {submitSuccess && (
                <div className="alert alert-success" role="alert">
                    {submitSuccess}
                </div>
            )}

            <form onSubmit={handleSubmit} style={{ maxWidth: "400px" }}>
                <div className="mb-3">
                    <label htmlFor="newPassword" className="form-label">
                        Nové heslo
                    </label>
                    <input
                        type="password"
                        id="newPassword"
                        className="form-control"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        required
                        minLength={8}
                    />
                </div>

                <div className="mb-3">
                    <label htmlFor="confirmPassword" className="form-label">
                        Potvrzení hesla
                    </label>
                    <input
                        type="password"
                        id="confirmPassword"
                        className="form-control"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        required
                        minLength={8}
                    />
                </div>

                <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={submitting}
                >
                    {submitting ? "Měním heslo..." : "Změnit heslo"}
                </button>
            </form>
        </div>
    );
};

export default ForgottenPasswordResetPage;
