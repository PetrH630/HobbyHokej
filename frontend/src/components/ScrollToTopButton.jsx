import { useEffect, useState } from "react";
import "./ScrollToTopButton.css";

/**
 * ScrollToTopButton
 *
 * React komponenta používaná ve frontend aplikaci.
 *
 * @param {Object} props vstupní hodnoty komponenty.
 */
const ScrollToTopButton = () => {
    const [visible, setVisible] = useState(false);

    useEffect(() => {
        
        const handleScroll = () => {

            setVisible(window.scrollY > 100);
        };

        window.addEventListener("scroll", handleScroll);
        return () => window.removeEventListener("scroll", handleScroll);
    }, []);

    const scrollToTop = () => {
        window.scrollTo({
            top: 0,
            behavior: "smooth",
        });
    };

    if (!visible) return null;

    return (
        <button
            className="scroll-to-top-btn"
            onClick={scrollToTop}
            aria-label="Zpět nahoru"
        >
            ↑
        </button>
    );
};

export default ScrollToTopButton;
