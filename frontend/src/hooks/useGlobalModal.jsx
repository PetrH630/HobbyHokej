import { useEffect } from "react";

/**
 * useGlobalModal
 *
 * Hook, který při otevření modalu přepne na `<body>` Bootstrap třídu `modal-open`.
 * Používá se u komponent, které renderují Bootstrap modal mimo standardní JS inicializaci.
 *
 * Vedlejší efekty:
 * - při `isOpen=true` přidá `modal-open`, po zavření nebo unmountu ji odstraní
 */
export const useGlobalModal = (isOpen) => {
  useEffect(() => {
    document.body.classList.toggle("modal-open", !!isOpen);

    return () => {
      document.body.classList.remove("modal-open");
    };
  }, [isOpen]);
};
