//jedna komponenta pro více použití (DRY).

import { Link } from "react-router-dom";
import { format } from "date-fns";

const MatchCard = ({ match, showPricePerPlayer = false }) => {
    const dateStr = format(new Date(match.dateTime), "dd.MM.yyyy HH:mm");

    return (
        <div className="card h-100 mb-3">
            <h5>{match.location}</h5>
            <p>{dateStr}</p>
            <p>{match.description}</p>
            {showPricePerPlayer && (
                <p><strong>Cena hráče:</strong> {match.pricePerRegisterdPlayer} Kč</p>
            )}
            <Link to={`/match/${match.id}`} className="btn btn-primary">
                Detail
            </Link>
        </div>
    );
};

export default MatchCard;
