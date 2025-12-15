import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Home from "./pages/Home";
import LoginPage from './pages/LoginPage';
import Navbar from './components/Navbar';

const App = () => (
  <BrowserRouter>
    
    <Navbar />
    <Routes>
      
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={<Home />} />
      
      
    </Routes>
  </BrowserRouter>
);

export default App;
