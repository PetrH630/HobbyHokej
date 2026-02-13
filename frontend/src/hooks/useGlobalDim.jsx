import { useEffect } from "react";

export const useGlobalDim = (isActive) => {
    useEffect(() => {
        if (isActive) document.body.classList.add("dim-open");
        else document.body.classList.remove("dim-open");

        return () => document.body.classList.remove("dim-open");
    }, [isActive]);
};
