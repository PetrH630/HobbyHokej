import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import "bootstrap/dist/css/bootstrap.min.css";
import App from "./App.jsx";
import { ImpersonationProvider } from "./context/ImpersonationContext.jsx";


import { AuthProvider } from "./hooks/useAuth.jsx";
import { NotificationProvider } from "./context/NotificationContext.jsx";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <AuthProvider>

      <ImpersonationProvider>
        <NotificationProvider>
          <App />
        </NotificationProvider>
      </ImpersonationProvider>


    </AuthProvider>
  </StrictMode>
);
