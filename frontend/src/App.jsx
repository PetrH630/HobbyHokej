// src/App.jsx
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import LoginPage from "./pages/LoginPage";
import SharedLayout from "./pages/SharedLayout";
import ScrollToTop from "./components/ScrollToTop";
import RegisterPage from "./pages/RegisterPage";
import RequireAuth from "./RequireAuth";
import Matches from "./pages/MatchesPage";
import Players from "./pages/PlayersPage";
import CreatePlayer from "./pages/CreatePlayer";
import MatchDetailPage from "./pages/MatchDetailPage";
import SettingsPage from "./pages/SettingsPage";
import { AuthProvider } from "./hooks/useAuth";
import ForgottenPasswordRequestPage from "./pages/ForgottenPasswordRequestPage";
import ForgottenPasswordResetPage from "./pages/ForgottenPasswordResetPage";
import { CurrentPlayerProvider } from "./hooks/useCurrentPlayer";
import { SeasonProvider } from "./hooks/useSeason";
import AdminPlayersInactivityPage from "./pages/AdminPlayersInactivityPage";
import MyInactivityPage from "./pages/MyInactivityPage";
import ScrollToTopButton from "./components/ScrollToTopButton";
import VerifyEmailPage from "./pages/VerifyEmailPage";

// ADMIN PAGES
import HomeDecider from "./pages/HomeDecider";
import AdminHomePage from "./pages/AdminHomePage";
import AdminPlayersPage from "./pages/AdminPlayersPage";
import RoleGuard from "./components/RoleGuard";

//ADMIN MATCHES
import AdminMatchesPage from "./pages/AdminMatchesPage";

//  ADMIN SEASONS
import AdminSeasonsPage from "./pages/AdminSeasonsPage";

//  ADMIN USERS
import AdminUsersPage from "./pages/AdminUsersPage";
import AdminUserDetailPage from "./pages/AdminUserDetailPage";

//  DEMO MODE CONTEXT
import { AppModeProvider } from "./context/AppModeContext";

//  veřejná úvodní stránka před loginem
import PublicLandingPage from "./pages/PublicLandingPage";

//  veřejný layout s topBarem
import PublicLayout from "./pages/PublicLayout";

const App = () => (
  <AuthProvider>
    <AppModeProvider>
      <BrowserRouter>
        <ScrollToTop resetPrefixes={["/"]} />

        <Routes>
          {/*  VEŘEJNÁ ČÁST – s HeaderTop */}
          <Route element={<PublicLayout />}>
            <Route path="/" element={<PublicLandingPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/verify" element={<VerifyEmailPage />} />
            <Route
              path="/forgotten-password"
              element={<ForgottenPasswordRequestPage />}
            />
            <Route
              path="/reset-password"
              element={<ForgottenPasswordResetPage />}
            />
          </Route>

          {/*  Chráněná část aplikace – se SharedLayout + Navbar */}
          <Route
            path="/app"
            element={
              <RequireAuth>
                <CurrentPlayerProvider>
                  <SeasonProvider>
                    <SharedLayout />
                  </SeasonProvider>
                </CurrentPlayerProvider>
              </RequireAuth>
            }
          >
            {/* index = "/app" */}
            <Route index element={<HomeDecider />} />

            {/* nested routy bez "/" */}
            <Route path="matches" element={<Matches />} />
            <Route path="players" element={<Players />} />
            <Route path="createPlayer" element={<CreatePlayer />} />
            <Route path="matches/:id" element={<MatchDetailPage />} />
            <Route path="settings" element={<SettingsPage />} />
            <Route path="my-inactivity" element={<MyInactivityPage />} />

            {/* ADMIN/MANAGER – SPRÁVA HRÁČŮ */}
            <Route
              path="admin/players"
              element={
                <RoleGuard roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
                  <AdminPlayersPage />
                </RoleGuard>
              }
            />

            {/* ADMIN/MANAGER – SPRÁVA ZÁPASŮ */}
            <Route
              path="admin/matches"
              element={
                <RoleGuard roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
                  <AdminMatchesPage />
                </RoleGuard>
              }
            />

            {/* ADMIN/MANAGER – SPRÁVA SEZÓN */}
            <Route
              path="admin/seasons"
              element={
                <RoleGuard roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
                  <AdminSeasonsPage />
                </RoleGuard>
              }
            />

            {/* ADMIN/MANAGER – SPRÁVA NEAKTIVIT */}
            <Route
              path="admin/inactivity"
              element={
                <RoleGuard roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
                  <AdminPlayersInactivityPage />
                </RoleGuard>
              }
            />

            {/* ADMIN – SPRÁVA UŽIVATELŮ */}
            <Route
              path="admin/users"
              element={
                <RoleGuard roles={["ROLE_ADMIN"]}>
                  <AdminUsersPage />
                </RoleGuard>
              }
            />
            <Route
              path="admin/users/:id"
              element={
                <RoleGuard roles={["ROLE_ADMIN"]}>
                  <AdminUserDetailPage />
                </RoleGuard>
              }
            />
          </Route>

          {/* fallback – cokoliv jiného → úvodní stránka */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>

        <ScrollToTopButton />
      </BrowserRouter>
    </AppModeProvider>
  </AuthProvider>
);

export default App;
