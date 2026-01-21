import { useAuth } from "../hooks/useAuth";

const RoleGuard = ({ roles, children }) => {
    const { user } = useAuth();

    if (!user) return null;
    if (!roles.includes(user.role)) return null;

    return children;
};

export default RoleGuard;
