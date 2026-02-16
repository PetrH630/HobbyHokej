import { useEffect } from "react";
import { useLocation } from "react-router-dom";

const ScrollToTop = ({ resetPrefixes = [] }) => {
    const { pathname } = useLocation();

    useEffect(() => {
           if (resetPrefixes.some(prefix => pathname.startsWith(prefix))) {
            window.scrollTo(0, 0);
        }       
    }, [pathname, resetPrefixes]);

    return null;
};

export default ScrollToTop;