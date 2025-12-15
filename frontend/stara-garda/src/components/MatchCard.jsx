import React from "react";
import { Link } from "react-router-dom";
import { format } from 'date-fns';


const MatchCard = ({ match }) => {
    if (!match) return null;

    const formattedDateTime = format(match.dateTime, 'dd.MM.yyyy HH:mm');

    return (
        <section className="mb-3">
            <div className="card h-100">
                <div className="card-body">
                    <h5 className="card-title">
                        Hokej #{match.id} – {match.location}
                    </h5>

                    <h6 className="card-subtitle mb-2 text-muted">
                        {formattedDateTime.toLocaleString()}

                    </h6>

                    {match.description && (
                        <p className="card-text">{match.description}</p>
                    )}

                    <p className="card-text">
                        <strong>Maximální hráči:</strong> {match.maxPlayers}
                    </p>

                    <p className="card-text">
                        <strong>Cena:</strong> {match.price} Kč
                    </p>

                    <Link
                        to={`/match/${match.id}`}
                        className="btn btn-primary mt-2"
                    >
                        Detail zápasu
                    </Link>
                </div>
            </div>
        </section>
    );
};


export default MatchCard;