import { Outlet } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import HeaderTop from "../components/HeaderTop";
import { useNotification } from "../context/NotificationContext";
import CookieConsentBanner from "../components/CookieConsentBanner";

const SharedLayout = () => {
    const { notification } = useNotification();

    return (
        <div className="layout">
            <HeaderTop />
            <Navbar />

            {/* NOTIFIKACE POD NAVBAREM */}
            {notification && (
                <div className="container mt-2">
                    <div
                        className={`alert alert-${notification.type} text-center`}
                        role="alert"
                    >
                        {notification.message}
                    </div>
                </div>
            )}

            <main className="content">
                <div className="container">
                    <Outlet />
                </div>
            </main>

            <Footer />

            {/*  COOKIE INFO – zobrazí se jen v přihlášené části */}
            <CookieConsentBanner />
        </div>
    );
};

export default SharedLayout;
