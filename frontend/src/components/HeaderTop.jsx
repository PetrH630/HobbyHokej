import React from "react";
import "./HeaderTop.css";
import { useDemoMode } from "../hooks/useDemoMode";

/**
 * HeaderTop
 *
 * Hlavičkový prvek stránky se zobrazením základních informací a navigačních prvků.
 *
 * @param {Object} props vstupní hodnoty komponenty.
 */
const HeaderTop = () => {
    const { demoMode, loading, error, isDemo } = useDemoMode();

    console.log("[HeaderTop] demoMode / loading / error:", {
        demoMode,
        loading,
        error,
    });

    const appText = `Aplikace${!loading && demoMode ? ": DEMO režim" : ""}`;

    return (
        <div className="header-top">
            <div className="container">
                <div className="row align-items-center py-1">

                    <div className="col-6 col-md-4 header-item text-start">
                        {appText}
                    </div>

                    {/* UPROSTŘED – Telefon (skrytý na malých) */}
                    <div className="d-none d-md-block col-md-4 header-item text-center">
                        📞 +420 123 456 789
                    </div>

                    <div className="col-6 col-md-4 header-item text-end">
                        ✉ petrhlista@seznam.cz
                    </div>
                </div>
            </div>
        </div>
    );
};

export default HeaderTop;
