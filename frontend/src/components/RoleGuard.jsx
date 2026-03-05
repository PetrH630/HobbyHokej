import { useAuth } from "../hooks/useAuth";

/**
 * RoleGuard
 *
 * React komponenta používaná ve frontend aplikaci.
 *
 * Props:
 * @param {Object} props.roles vstupní hodnota komponenty.
 * @param {Object} props.children vstupní hodnota komponenty.
 */
const RoleGuard = ({ roles, children }) => {
    const { user } = useAuth();

    if (!user) return null;

    const hasAccess = roles.includes(user.role);
    if (!hasAccess) return null;

    return children;
};

export default RoleGuard;
