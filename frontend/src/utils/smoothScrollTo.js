// src/utils/smoothScroll.js

export const smoothScrollTo = (targetY, duration = 800) => {
    const startY = window.scrollY;
    const diff = targetY - startY;
    const startTime = performance.now();

    const easeInOut = (t) => {
        return t < 0.5
            ? 2 * t * t
            : 1 - Math.pow(-2 * t + 2, 2) / 2;
    };

    const step = (currentTime) => {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const eased = easeInOut(progress);

        window.scrollTo(0, startY + diff * eased);

        if (progress < 1) {
            requestAnimationFrame(step);
        }
    };

    requestAnimationFrame(step);
};
