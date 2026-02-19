const clamp = (v, min, max) => Math.min(max, Math.max(min, v));
const lerp = (a, b, t) => a + (b - a) * t;

// 0% = red (0°), 100% = green (120°)
const percentToColor = (p) => {
    const percent = Math.round(clamp(p, 0, 1) * 100);

    // zaokrouhlení na desítky
    const bucket = Math.floor(percent / 10) * 10;

    const colorScale = {
        0: "#b30000",
        10: "#cc0000",
        20: "#e60000",
        30: "#ff3300",
        40: "#ff6600",
        50: "#ff9900",
        60: "#ffcc00",
        70: "#cccc00",
        80: "#99cc00",
        90: "#66cc00",
        100: "#2eb82e",
    };

    return colorScale[bucket] || "#2eb82e";
};


const CapacityRing = ({
    value = 0,
    max = 0,
    size = 34,      // větší
    stroke = 6,     // tloušťka progress
}) => {
    const safeMax = max > 0 ? max : 0;
    const ratio = safeMax > 0 ? clamp(value / safeMax, 0, 1) : 0;

    const r = (size - stroke - 4) / 2; // -4 kvůli outline
    const c = 2 * Math.PI * r;
    const dashOffset = c * (1 - ratio);
    const center = size / 2;

    const color = percentToColor(ratio);

    return (
        <svg
            width={size}
            height={size}
            viewBox={`0 0 ${size} ${size}`}
            className="capacity-ring"
        >
            {/* 1️⃣ Vnější černý okraj */}
            <circle
                cx={center}
                cy={center}
                r={r + stroke / 2 + 1}
                fill="none"
                stroke="black"
                strokeWidth="1"
            />

            {/* 2️⃣ Bílá neobsazená část (pozadí) */}
            <circle
                cx={center}
                cy={center}
                r={r}
                fill="none"
                stroke="white"
                strokeWidth={stroke}
            />

            {/* 3️⃣ Barevná obsazená část */}
            <circle
                cx={center}
                cy={center}
                r={r}
                fill="none"
                stroke={color}
                strokeWidth={stroke}
                strokeLinecap="round"
                strokeDasharray={c}
                strokeDashoffset={dashOffset}
                transform={`rotate(-90 ${center} ${center})`}
                style={{
                    transition: "stroke-dashoffset 0.4s ease, stroke 0.4s ease",
                }}
            />

            {/* 4️⃣ Vnitřní černý okraj */}
            <circle
                cx={center}
                cy={center}
                r={r - stroke / 2 - 1}
                fill="none"
                stroke="black"
                strokeWidth="1"
            />
        </svg>
    );
};

export default CapacityRing;
