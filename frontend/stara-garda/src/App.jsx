import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Home from "./pages/Home";
import LoginPage from './pages/LoginPage';
import Navbar from './components/Navbar';
import SharedLayout from "./pages/SharedLayout"
import ScrollToTop from "./components/ScrollToTop";
import RegisterPage from "./pages/RegisterPage";


const App = () => (
  <BrowserRouter>
    <ScrollToTop resetPrefixes={["/"]} />
    <Routes>

      <Route path="/" element={<SharedLayout />}>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route index element={<Home />} />
      
      

      </Route>
    </Routes>
  </BrowserRouter>
);

export default App;
