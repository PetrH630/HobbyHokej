import { useEffect, useState } from "react";

const STORAGE_KEY = "cookie_consent_accepted";

const CookieConsentBanner = () => {
    const [visible, setVisible] = useState(false);

    useEffect(() => {
        const accepted = localStorage.getItem(STORAGE_KEY);
        if (!accepted) {
            setVisible(true);
        }
    }, []);

    const handleAccept = () => {
        localStorage.setItem(STORAGE_KEY, "true");
        setVisible(false);
    };

    if (!visible) return null;

    return (
        <div
            className="position-fixed bottom-0 start-0 end-0 bg-dark text-white p-3 shadow-lg"
            style={{ zIndex: 1055 }}
        >
            <div className="container d-flex flex-column flex-md-row justify-content-between align-items-center gap-3">
                <div>
                    <strong>Používání cookies</strong>
                    <div className="small">
                        Tato aplikace používá nezbytné technické cookies
                        pro správné fungování (přihlášení, session,
                        bezpečnostní ochranu).
                        Nepoužíváme marketingové ani sledovací cookies.
                    </div>
                </div>

                <button
                    className="btn btn-success btn-sm"
                    onClick={handleAccept}
                >
                    Rozumím
                </button>
            </div>
        </div>
    );
};

export default CookieConsentBanner;
