
/**
 * smoothScrollTo
 *
 * Bezpečný helper pro plynulé odscrollování na konkrétní pozici nebo element.
 * Používá se při návratu na seznamy (např. po detailu zápasu) a pro lepší UX na mobilu.
 */
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
