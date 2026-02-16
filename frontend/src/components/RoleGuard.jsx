import { useAuth } from "../hooks/useAuth";

const RoleGuard = ({ roles, children }) => {
    const { user } = useAuth();

    if (!user) return null;

    const hasAccess = roles.includes(user.role);
    if (!hasAccess) return null;

    return children;
};

export default RoleGuard;
