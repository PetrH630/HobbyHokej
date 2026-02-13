import { useEffect } from "react";
import * as bootstrap from "bootstrap";

export const useBootstrapTooltip = () => {
    useEffect(() => {
        const tooltipTriggerList = [].slice.call(
            document.querySelectorAll('[data-bs-toggle="tooltip"]')
        );

        const tooltipList = tooltipTriggerList.map(
            (tooltipTriggerEl) =>
                new bootstrap.Tooltip(tooltipTriggerEl)
        );

        return () => {
            tooltipList.forEach((tooltip) => tooltip.dispose());
        };
    }, []);
};
