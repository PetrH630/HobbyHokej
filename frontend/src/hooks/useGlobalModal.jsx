// src/hooks/useGlobalModal.js
import { useEffect } from "react";

export const useGlobalModal = (isOpen) => {
  useEffect(() => {
    document.body.classList.toggle("modal-open", !!isOpen);

    return () => {
      document.body.classList.remove("modal-open");
    };
  }, [isOpen]);
};
