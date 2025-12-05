import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Home from "./pages/Home";
import MatchDetail from './components/MatchDetail';

const App = () => (
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/match/:matchId" element={<MatchDetail />} />
    </Routes>
  </BrowserRouter>
);

export default App;
