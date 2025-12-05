import React from 'react';
import TestConnection from "../components/TestConnection"
import MatchDetail from '../components/MatchDetail';
import MatchCard from '../components/MatchCard';
import Matches from '../components/Matches';


const Home = () => {
    return (
        <div className="container mt-4">
            <h1>Vítejte ve Stará Garda</h1>
            <p>Frontend připravený s React + Vite + Bootstrap.</p>
            <TestConnection />
            {/*<MatchDetail matchId={3}/>*/}
           <MatchCard />
           <Matches />
           

        </div>
    );
};

export default Home;