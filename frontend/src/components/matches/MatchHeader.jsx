// src/components/MatchHeader.jsx
//import "./MatchHeader.css";

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
        dateTime: `${datePart}  ${timePart}`,
    };
};

const MatchHeader = ({ match }) => {
    const formatted = formatDateTime(match?.dateTime);

    if (!formatted) return null;

    return (
        <div className="match-header text-center mb-3">
            <h4 className="match-header-day">
                {formatted.day} {"  #"}{match.matchNumber}
            </h4>

            <h5 className="match-header-date">
                {formatted.dateTime}
            </h5>

            <p className="match-header-location">
                {match.location}
            </p>
        </div>
    );
};

export default MatchHeader;
