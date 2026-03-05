import { useEffect } from "react";

/**
 * useGlobalDim
 *
 * Hook pro globální ztmavení pozadí ("dim") při otevřených překryvných vrstvách.
 * Přidává/odebírá CSS třídu na `<body>` podle hodnoty `isDimmed`.
 */
export const useGlobalDim = (isActive) => {
    useEffect(() => {
        if (isActive) document.body.classList.add("dim-open");
        else document.body.classList.remove("dim-open");

        return () => document.body.classList.remove("dim-open");
    }, [isActive]);
};
