import { useEffect } from "react";
import { useLocation } from "react-router-dom";

const ScrollToTop = ({ resetPrefixes = [] }) => {
    const { pathname } = useLocation();

    useEffect(() => {
        // Pokud aktuální path začíná některým prefixem → scroll na začátek
        if (resetPrefixes.some(prefix => pathname.startsWith(prefix))) {
            window.scrollTo(0, 0);
        }
        // Jinak nech scroll tam, kde byl
    }, [pathname, resetPrefixes]);

    return null;
};

export default ScrollToTop;