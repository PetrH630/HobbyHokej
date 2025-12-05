import React from "react";
import { Link } from "react-router-dom";

const MatchCard = ({ match }) => {
    if (!match) return null;

    return (
        <Link to={`/match/${match.id}`} style={{ textDecoration: "none", color: "inherit" }}>
            <div className="card mb-3">
                <div className="card-body">
                    <h5 className="card-title">Zápas #{match.id} - {match.location}</h5>
                    <h6 className="card-subtitle mb-2 text-muted">
                        {new Date(match.dateTime).toLocaleString()}
                    </h6>
                    {match.description && <p className="card-text">{match.description}</p>}
                    <p className="card-text"><strong>Maximální hráči:</strong> {match.maxPlayers}</p>
                    <p className="card-text"><strong>Cena:</strong> {match.price} Kč</p>
                </div>
            </div>
        </Link>
    );
};

export default MatchCard;
