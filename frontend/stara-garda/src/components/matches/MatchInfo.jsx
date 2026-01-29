// src/components/MatchInfo.jsx

const MatchInfo = ({ match }) => {
    return (
        <div className="card">
            <div className="card-body">
                {match.description && (
                    <p className="card-text mb-2">
                        <strong>Popis: </strong>
                        {match.description}
                    </p>
                )}

                <p className="card-text mb-2">
                    <strong>Hráči: </strong>
                    {match.inGamePlayers} / {match.maxPlayers}
                </p>
                <p className="card-text mb-2">
                    <strong>Hráči: </strong>
                    {match.inGamePlayersDark} / {match.maxPlayersLight}
                </p>

                {match.price != null && (
                    <p className="card-text mb-2">
                        <strong>Cena zápasu: </strong>
                        {match.price} Kč
                    </p>
                )}

                {match.pricePerRegisteredPlayer != null && (
                    <p className="card-text mb-2">
                        <strong>Cena / hráč: </strong>
                        {match.pricePerRegisteredPlayer.toFixed(0)} Kč
                    </p>
                )}

                
            </div>
        </div>
    );
};

export default MatchInfo;
