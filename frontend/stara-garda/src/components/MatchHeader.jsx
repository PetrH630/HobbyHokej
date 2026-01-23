

const formatDateTime = (dateTime) => {
    if (!dateTime) return null;

    const iso = dateTime.replace(" ", "T");
    const d = new Date(iso);

    const dayName = new Intl.DateTimeFormat("cs-CZ", {
        weekday: "long",
    }).format(d);

    const datePart = new Intl.DateTimeFormat("cs-CZ", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
    }).format(d);

    const timePart = d.toLocaleTimeString("cs-CZ", {
        hour: "2-digit",
        minute: "2-digit",
    });

    return {
        day: dayName.charAt(0).toUpperCase() + dayName.slice(1),
        dateTime: `${datePart} · ${timePart}`,
    };
};


const MatchHeader = ({ match }) => {
    const formatted = formatDateTime(match.dateTime);
    return (
        <div className="mb-3 text-center">
        { formatted && (
            <>
                <h4 className="card-title text-muted text-center mb-1 match-day">
                    {formatted.day}
                </h4>

                <h5 className="text-center mb-2 match-date">
                    {formatted.dateTime}
                </h5>
            </>
        )}
        
            <p className="mb-1">
                <strong>Místo:</strong> {match.location}
            </p>
            
        </div>
    );
};

export default MatchHeader;
