import { useEffect } from "react";
import * as bootstrap from "bootstrap";

/**
 * useBootstrapTooltip
 *
 * Inicializuje Bootstrap tooltips pro prvky s atributem `data-bs-toggle="tooltip"`.
 * Hook se typicky volá v komponentách, které tooltipy renderují, aby Bootstrap vytvořil instance a spravoval jejich lifecycle.
 *
 * Vedlejší efekty:
 * - na mountu vytvoří `bootstrap.Tooltip` instance
 * - na unmountu je korektně zlikviduje přes `dispose()`
 */
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
