import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import "bootstrap/dist/css/bootstrap.min.css";
import App from "./App.jsx";

// Import AuthProvider a BrowserRouter ze správných cest
import { AuthProvider } from "./hooks/useAuth.jsx";
import { BrowserRouter } from "react-router-dom";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <AuthProvider>
      
     
        <App />
      
      
    </AuthProvider>
  </StrictMode>
);
