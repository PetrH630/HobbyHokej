import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Home from "./pages/Home";
import LoginPage from './pages/LoginPage';
import Navbar from './components/Navbar';
import SharedLayout from "./pages/SharedLayout"
import ScrollToTop from "./components/ScrollToTop";
import RegisterPage from "./pages/RegisterPage";
import RequireAuth from "./RequireAuth";
import Matches from "./pages/Matches";
import Players from "./pages/Players";
import { CurrentPlayerProvider } from "./hooks/useCurrentPlayer";


const App = () => (
  <BrowserRouter>
    <ScrollToTop resetPrefixes={["/"]} />

    <Routes>
      {/*Veřejné stránky – BEZ SharedLayout */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Chráněná část aplikace – se SharedLayout + Navbar*/}
      <Route
        path="/"
        element={
          <RequireAuth>
            <CurrentPlayerProvider>
              <SharedLayout />
            </CurrentPlayerProvider>
          </RequireAuth>
        }
      >
        {/* index = "/" */}
        {/* sem můžeš přidávat další chráněné stránky, např.: */}
        <Route index element={<Home />} />
        <Route path="/matches" element={<Matches />} />
        <Route path="/players" element={<Players />} />



      </Route>

      {/* fallback – cokoliv jiného → login */}

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  </BrowserRouter>
);

export default App;
