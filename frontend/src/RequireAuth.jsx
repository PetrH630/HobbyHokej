// chráněná routa s využitím useAuth()
//Zabezpečení: nerenderuje UI dokud nevíme stav. Akce auth je pouze v hooku, ne v komponentě.

import { Navigate } from "react-router-dom";
import { useAuth } from "./hooks/useAuth";

/**
 * RequireAuth
 *
 * UI komponenta.
 *
 * Props:
 * @param {Object} props vstupní hodnoty komponenty
 * @param {*} props.children vstupní hodnota komponenty
 */
const RequireAuth = ({ children }) => {
    const { user, loading } = useAuth();

    if (loading) return <p>Ověřuji přihlášení…</p>;

    return user ? children : <Navigate to="/login" replace />;
};

export default RequireAuth;

