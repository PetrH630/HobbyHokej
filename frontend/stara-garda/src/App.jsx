import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Home from "./pages/Home";
import LoginPage from './pages/LoginPage';
import SharedLayout from "./pages/SharedLayout";
import ScrollToTop from "./components/ScrollToTop";
import RegisterPage from "./pages/RegisterPage";
import RequireAuth from "./RequireAuth";
import Matches from "./pages/MatchesPage";
import Players from "./pages/PlayersPage";
import CreatePlayer from "./pages/CreatePlayer";
import MatchDetailPage from "./pages/MatchDetailPage";
import { AuthProvider } from "./hooks/useAuth";
import ForgottenPasswordRequestPage from "./pages/ForgottenPasswordRequestPage";
import ForgottenPasswordResetPage from "./pages/ForgottenPasswordResetPage";

import { CurrentPlayerProvider } from "./hooks/useCurrentPlayer";

// 👇 ADMIN PAGES
import AdminPlayersPage from "./pages/AdminPlayersPage";
import RoleGuard from "./components/RoleGuard";

// 👇 NOVÉ – ADMIN USERS
import AdminUsersPage from "./pages/AdminUsersPage";
import AdminUserDetailPage from "./pages/AdminUserDetailPage";

const App = () => (
  <AuthProvider>
    <BrowserRouter>
      <ScrollToTop resetPrefixes={["/"]} />

      <Routes>
        {/* Veřejné stránky – BEZ SharedLayout */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgotten-password" element={<ForgottenPasswordRequestPage />} />
        <Route path="/reset-password" element={<ForgottenPasswordResetPage />} />

        {/* Chráněná část aplikace – se SharedLayout + Navbar */}
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
          <Route index element={<Home />} />
          <Route path="/matches" element={<Matches />} />
          <Route path="/players" element={<Players />} />
          <Route path="/createPlayer" element={<CreatePlayer />} />
          <Route path="/matches/:id" element={<MatchDetailPage />} />

          {/* ADMIN/MANAGER – SPRÁVA HRÁČŮ */}
          <Route
            path="/admin/players"
            element={
              <RoleGuard roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
                <AdminPlayersPage />
              </RoleGuard>
            }
          />

          {/* ADMIN – SPRÁVA UŽIVATELŮ */}
          <Route
            path="/admin/users"
            element={
              <RoleGuard roles={["ROLE_ADMIN"]}>
                <AdminUsersPage />
              </RoleGuard>
            }
          />
          <Route
            path="/admin/users/:id"
            element={
              <RoleGuard roles={["ROLE_ADMIN"]}>
                <AdminUserDetailPage />
              </RoleGuard>
            }
          />
        </Route>

        {/* fallback – cokoliv jiného → login */}
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  </AuthProvider>
);

export default App;
